#!/usr/bin/env sh

MAIN_IMAGE='ggbot'
DATABASE_CONTAINER="${MAIN_IMAGE}-test-database"
TEMP_DIRECTORIES='.gradle build tmp'

log() {
    COLOUR_RESET='\033[0m'
    PURPLE_BOLD='\033[1;35m'
    echo "${PURPLE_BOLD}==> ${1} <==${COLOUR_RESET}"
}

warn() {
    COLOUR_RESET='\033[0m'
    YELLOW_BOLD='\033[1;33m'
    echo "${YELLOW_BOLD}==> ${1} <==${COLOUR_RESET}"
}

error() {
    COLOUR_RESET='\033[0m'
    RED_BOLD='\033[1;31m'
    echo "${RED_BOLD}==> ${1} <==${COLOUR_RESET}"
    exit 1
}

./bin/database.sh --apply-migrations --container-name "$DATABASE_CONTAINER"

log 'Running integration tests'
if ! ./gradlew integrationTest --info; then
    tests_failed='true'
    warn 'One or more integration tests failed'
fi

log "Stopping '${DATABASE_CONTAINER}' container"
docker container stop "$DATABASE_CONTAINER"

log "Removing '${DATABASE_CONTAINER}' container"
docker container rm --volumes "$DATABASE_CONTAINER"

for directory in $TEMP_DIRECTORIES; do
    log "Removing '${directory}' temp directory"
    rm -rf "$directory"
done

if [ 'true' = "$tests_failed" ]; then
    error 'One or more integration tests failed'
fi
