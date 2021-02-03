#!/usr/bin/env sh

MAIN_IMAGE='ggbot'
MAIN_CONTAINER="$MAIN_IMAGE"
SPRING_PORT='8080'
DEBUG_PORT='8000'

CACHE_VOLUME='gradle-build-cache'
NETWORK="${MAIN_IMAGE}-network"

BUILD_IMAGE="${MAIN_IMAGE}-gradle-build"
BUILD_CONTAINER="$BUILD_IMAGE"
BUILD_COMMAND='gradle build --stacktrace --exclude-task test'

DATABASE_CONTAINER="${MAIN_IMAGE}-database"

log() {
    COLOUR_RESET='\033[0m'
    PURPLE_BOLD='\033[1;35m'
    echo "${PURPLE_BOLD}==> ${1} <==${COLOUR_RESET}"
}

error() {
    COLOUR_RESET='\033[0m'
    RED_BOLD='\033[1;31m'
    echo "${RED_BOLD}==> ${1} <==${COLOUR_RESET}"
    exit 1
}

while [ "$#" -gt 0 ]; do
    case $1 in
    --apply-migrations)
        _apply_migrations='--apply-migrations'
        ;;
    --cache-from)
        shift
        _cache_from="${_cache_from} --cache-from=${1}"
        ;;
    --debug)
        _debug='debug=true'
        ;;
    --detach)
        _detach='true'
        ;;
    --no-cache)
        _no_cache='--no-cache'
        ;;
    --suspend)
        _suspend='suspend=true'
        ;;
    *)
        error "Unknown option: '${1}'"
        ;;
    esac
    shift
done

if [ '--no-cache' = "$_no_cache" ]; then
    _cache_from=''
fi

interactive='--interactive --tty'
if [ 'true' = "$_detach" ]; then
    interactive=''
fi

##############################################################################
##################### Prepare miscellaneous Docker items #####################
##############################################################################
log "Creating '${CACHE_VOLUME}' volume"
docker volume create --name "$CACHE_VOLUME"

log "Creating '${MAIN_IMAGE}' network"
docker network create --driver bridge "$NETWORK"

##############################################################################
########################## Build and export the JAR ##########################
##############################################################################
log "Building '${BUILD_IMAGE}' image"
# shellcheck disable=SC2086
docker build $_cache_from $_no_cache --tag "$BUILD_IMAGE" --file ./docker/Dockerfile-gradle .

log "Running '${BUILD_IMAGE}' image"
# shellcheck disable=SC2086
docker run $interactive \
    --name "$BUILD_CONTAINER" \
    --volume "${CACHE_VOLUME}:/home/gradle/.gradle" \
    --user gradle \
    "$BUILD_IMAGE" \
    $BUILD_COMMAND

log "Copying JAR from '${BUILD_CONTAINER}'"
rm -r ./build/libs >/dev/null 2>&1
mkdir -p ./build/libs
docker cp "${BUILD_CONTAINER}:/home/gradle/project/build/libs" ./build \
    || error "Failed to copy JAR out ${BUILD_CONTAINER}"
mv ./build/libs/*.jar ./build/libs/app.jar

##############################################################################
############### Build the main image and create the container ################
##############################################################################
build_args="${_debug} ${_suspend}"
# shellcheck disable=SC2086,SC2116
build_args="$(echo $build_args)" # Trim white space
if [ -n "$build_args" ]; then
    builder=''
    for arg in $build_args; do
        builder="${builder} --build-arg ${arg}"
    done
    _build_args="$builder"
fi

log "Building '${MAIN_IMAGE}' image"
# shellcheck disable=SC2086
docker build $_cache_from $_no_cache --tag "$MAIN_IMAGE" --file ./docker/Dockerfile $_build_args .

log "Creating '${MAIN_CONTAINER}' container"
publish_main="--publish ${SPRING_PORT}:${SPRING_PORT}"
if [ 'debug=true' = "$_debug" ] || [ 'suspend=true' = "$_suspend" ]; then
    publish_main="${publish_main} --publish ${DEBUG_PORT}:${DEBUG_PORT}"
fi
# shellcheck disable=SC2086
docker create $interactive \
    $publish_main \
    --name "$MAIN_CONTAINER" \
    --network "$NETWORK" \
    "$MAIN_IMAGE"

log "Copying JAR into '${MAIN_CONTAINER}'"
docker cp ./build/libs/app.jar "${MAIN_CONTAINER}:/home/project/app.jar"

##############################################################################
########################## Run the database image ############################
##############################################################################
if ! ./bin/database.sh $_apply_migrations --container-name "$DATABASE_CONTAINER" --network "$NETWORK"; then
    error 'Database startup failed'
fi

##############################################################################
############################ Start the main image ############################
##############################################################################
log "Starting '${MAIN_CONTAINER}'"
attach_interactive='--attach --interactive'
if [ 'true' = "$_detach" ]; then
    attach_interactive=''
fi
# shellcheck disable=SC2086
docker start $attach_interactive "$MAIN_CONTAINER"
