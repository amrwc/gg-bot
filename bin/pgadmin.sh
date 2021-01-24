#!/usr/bin/env sh

MAIN_IMAGE='renameme'
NETWORK="${MAIN_IMAGE}-network"
PGADMIN_IMAGE='dpage/pgadmin4'
CONTAINER_NAME='pgadmin'

PORT='5050'
USERNAME='user@domain.com'
PASSWORD='SuperSecret'

NOFORMAT='\033[0m' PURPLE='\033[0;35m'

while [ "$#" -gt 0 ]; do
    case $1 in
    --detach)
        _detach='true'
        ;;
    *)
        echo "Unknown option: ${1}"
        exit 1
        ;;
    esac
    shift
done

[ 'true' = "$_detach" ] && interactive='' || interactive='--interactive'

echo "${PURPLE}Creating '${PGADMIN_IMAGE}' container, name: ${CONTAINER_NAME}${NOFORMAT}"
docker create --interactive --tty \
    --env "PGADMIN_DEFAULT_EMAIL=${USERNAME}" \
    --env "PGADMIN_DEFAULT_PASSWORD=${PASSWORD}" \
    --publish "${PORT}:80" \
    --network "$NETWORK" \
    --name "$CONTAINER_NAME" \
    "$PGADMIN_IMAGE"

echo "${PURPLE}Running '${PGADMIN_IMAGE}' container, name: ${CONTAINER_NAME}${NOFORMAT}"
# shellcheck disable=SC2086
docker start $interactive "$CONTAINER_NAME"
