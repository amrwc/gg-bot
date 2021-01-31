#!/usr/bin/env sh

DATABASE_IMAGE='postgres:latest'
DATABASE_PORT='5432'

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
        _apply_migrations='true'
        ;;
    --container-name)
        shift
        _container_name="$1"
        ;;
    --network)
        shift
        _network="--network ${1}"
        ;;
    *)
        error "Unknown option: '${1}'"
        ;;
    esac
    shift
done

if [ -z "$_container_name" ]; then
    error 'Undefined container name'
fi

log "Running '${DATABASE_IMAGE}' container, name: ${_container_name}"
# shellcheck disable=SC2086
docker run --detach \
    --name "$_container_name" \
    --publish "${DATABASE_PORT}:${DATABASE_PORT}" \
    $_network \
    --env-file ./docker/postgres-envars.list \
    "$DATABASE_IMAGE"

if [ 'true' = "$_apply_migrations" ]; then
    log 'Applying database migrations'
    sleep 3 # Wait for the database to come up
    ./bin/apply_migrations.sh
fi
