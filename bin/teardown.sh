#!/usr/bin/env sh

MAIN_IMAGE='ggbot'
MAIN_CONTAINER="$MAIN_IMAGE"

BUILD_IMAGE="${MAIN_IMAGE}-gradle-build"
BUILD_CONTAINER="$BUILD_IMAGE"

DATABASE_CONTAINER="${MAIN_IMAGE}-database"

NETWORK="${MAIN_IMAGE}-network"
CACHE_VOLUME='gradle-build-cache'
TEMP_DIRECTORIES='.gradle build tmp'

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
    --include-cache)
        _include_cache='true'
        ;;
    --include-db)
        _include_db='true'
        ;;
    --include-tmp)
        _include_tmp='true'
        ;;
    *)
        error "Unknown option: '${1}'"
        ;;
    esac
    shift
done

log "Stopping '${BUILD_CONTAINER}' container"
docker container stop "$BUILD_CONTAINER"
log "Removing '${BUILD_CONTAINER}' container"
docker container rm "$BUILD_CONTAINER"
log "Removing '${BUILD_IMAGE}' image"
docker image rm "$BUILD_IMAGE"

log "Stopping '${MAIN_CONTAINER}' container"
docker container stop "$MAIN_CONTAINER"
log "Removing '${MAIN_CONTAINER}' container"
docker container rm --volumes "$MAIN_CONTAINER"
log "Removing '${MAIN_IMAGE}' image"
docker image rm "$MAIN_IMAGE"

if [ 'true' = "$_include_db" ]; then
    log "Stopping '${DATABASE_CONTAINER}' container"
    docker container stop "$DATABASE_CONTAINER"
    log "Removing '${DATABASE_CONTAINER}' container"
    docker container rm --volumes "$DATABASE_CONTAINER"
fi

# Since nothing exists within the network at this point, remove it
if [ 'true' = "$_include_db" ]; then
    log "Removing '${NETWORK}' network"
    docker network rm "$NETWORK"
fi

if [ 'true' = "$_include_cache" ]; then
    log "Removing '${CACHE_VOLUME}' volume"
    docker volume rm "$CACHE_VOLUME"
fi

if [ 'true' = "$_include_tmp" ]; then
    for directory in $TEMP_DIRECTORIES; do
        log "Removing '${directory}' temp directory"
        rm -rf "$directory"
    done
fi
