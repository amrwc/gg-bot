#!/usr/bin/env sh

MAIN_IMAGE='renameme'
NETWORK="${MAIN_IMAGE}-network"
DATABASE="${MAIN_IMAGE}-database"
CACHE_VOLUME='gradle-cache'
TEMP_DIRECTORIES='.gradle build'

NOFORMAT='\033[0m' PURPLE='\033[0;35m'

while [ "$#" -gt 0 ]; do
    case $1 in
    --exclude-db)
        _exclude_db='true'
        ;;
    --include-cache)
        _include_cache='true'
        ;;
    *)
        echo "Unknown option: ${1}"
        exit 1
        ;;
    esac
    shift
done

echo "${PURPLE}Stopping '${MAIN_IMAGE}' container${NOFORMAT}"
docker container stop "$MAIN_IMAGE"
echo "${PURPLE}Removing '${MAIN_IMAGE}' container${NOFORMAT}"
docker container rm --volumes "$MAIN_IMAGE"
echo "${PURPLE}Removing '${MAIN_IMAGE}' image${NOFORMAT}"
docker image rm "$MAIN_IMAGE"

echo "${PURPLE}Stopping '${DATABASE}' container${NOFORMAT}"
docker container stop "$DATABASE"
if [ 'true' != "$_exclude_db" ]; then
    echo "${PURPLE}Removing '${DATABASE}' container${NOFORMAT}"
    docker container rm --volumes "$DATABASE"
fi

echo "${PURPLE}Removing '${NETWORK}' network${NOFORMAT}"
docker network rm "$NETWORK"

if [ 'true' = "$_include_cache" ]; then
    echo "${PURPLE}Removing '${CACHE_VOLUME}' volume${NOFORMAT}"
    docker volume rm "$CACHE_VOLUME"
fi

for directory in $TEMP_DIRECTORIES; do
    echo "${PURPLE}Removing '${directory}'${NOFORMAT}"
    rm -rf "$directory"
done
