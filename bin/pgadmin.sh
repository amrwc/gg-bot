#!/usr/bin/env sh

MAIN_IMAGE='ggbot'
NETWORK="${MAIN_IMAGE}-network"
PGADMIN_IMAGE='dpage/pgadmin4'
CONTAINER_NAME='pgadmin'

PORT='5050'
USERNAME='user@domain.com'
PASSWORD='SuperSecret'

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
    --detach)
        _detach='true'
        ;;
    *)
        error "Unknown option: '${1}'"
        ;;
    esac
    shift
done

[ 'true' = "$_detach" ] && interactive='' || interactive='--interactive'

log "Creating '${PGADMIN_IMAGE}' container, name: ${CONTAINER_NAME}"
docker create --interactive --tty \
    --env "PGADMIN_DEFAULT_EMAIL=${USERNAME}" \
    --env "PGADMIN_DEFAULT_PASSWORD=${PASSWORD}" \
    --publish "${PORT}:80" \
    --network "$NETWORK" \
    --name "$CONTAINER_NAME" \
    "$PGADMIN_IMAGE"

log "Running '${PGADMIN_IMAGE}' container, name: ${CONTAINER_NAME}"
# shellcheck disable=SC2086
docker start $interactive "$CONTAINER_NAME"
