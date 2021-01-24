#!/usr/bin/env sh

CACHE_VOLUME='gradle-cache'
MAIN_IMAGE='renameme'
NETWORK="${MAIN_IMAGE}-network"
DATABASE="${MAIN_IMAGE}-database"
DATABASE_IMAGE='postgres:latest'

DATABASE_PORT='5432'
SPRING_PORT='8080'
DEBUG_PORT='8000'

NOFORMAT='\033[0m' PURPLE='\033[0;35m'

while [ "$#" -gt 0 ]; do
    case $1 in
    --cache-from)
        shift
        _cache_from="${_cache_from} --cache-from=${1}"
        ;;
    --debug)
        _debug='debug=true'
        ;;
    --no-cache)
        _no_cache='--no-cache'
        ;;
    --suspend)
        _suspend='suspend=true'
        ;;
    *)
        echo "Unknown option: ${1}"
        exit 1
        ;;
    esac
    shift
done

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

_publish_main="--publish ${SPRING_PORT}:${SPRING_PORT}"
if [ 'debug=true' = "$_debug" ] || [ 'suspend=true' = "$_suspend" ]; then
    _publish_main="${_publish_main} --publish ${DEBUG_PORT}:${DEBUG_PORT}"
fi

if [ '--no-cache' = "$_no_cache" ]; then
    _cache_from=''
fi

echo "${PURPLE}Creating '${CACHE_VOLUME}' volume${NOFORMAT}"
docker volume create --name "$CACHE_VOLUME"

echo "${PURPLE}Building '${MAIN_IMAGE}' image${NOFORMAT}"
# shellcheck disable=SC2086
docker build $_cache_from $_no_cache --tag "${MAIN_IMAGE}:latest" --file ./docker/Dockerfile $_build_args .

echo "${PURPLE}Creating '${NETWORK}' network${NOFORMAT}"
docker network create --driver bridge "$NETWORK"

echo "${PURPLE}Creating '${DATABASE_IMAGE}' container, name: ${DATABASE}${NOFORMAT}"
docker create \
    --publish "${DATABASE_PORT}:${DATABASE_PORT}" \
    --network="$NETWORK" \
    --env-file ./docker/postgres-envars.list \
    --name "$DATABASE" \
    "$DATABASE_IMAGE"

echo "${PURPLE}Creating '${MAIN_IMAGE}' container, name: ${MAIN_IMAGE}${NOFORMAT}"
# shellcheck disable=SC2086
docker create --interactive --tty \
    $_publish_main \
    --network="$NETWORK" \
    --name "$MAIN_IMAGE" \
    "$MAIN_IMAGE"
