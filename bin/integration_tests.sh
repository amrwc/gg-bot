#!/usr/bin/env sh

MAIN_IMAGE='ggbot'

DATABASE="${MAIN_IMAGE}-test-database"
DATABASE_IMAGE='postgres:latest'
DATABASE_PORT='5432'

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

log "Running '${DATABASE_IMAGE}' container, name: ${DATABASE}"
docker run --detach \
    --publish "${DATABASE_PORT}:${DATABASE_PORT}" \
    --env-file ./docker/postgres-envars.list \
    --name "$DATABASE" \
    "$DATABASE_IMAGE"

log 'Applying database migrations'
sleep 3 # Wait for the database to come up
./bin/apply_migrations.sh

log 'Running integration tests'
if ! ./gradlew integrationTest; then
    tests_failed='true'
    warn 'One or more integration tests failed'
fi

log "Stopping '${DATABASE}' container"
docker container stop "$DATABASE"

log "Removing '${DATABASE}' container"
docker container rm --volumes "$DATABASE"

for directory in $TEMP_DIRECTORIES; do
    log "Removing '${directory}' temp directory"
    rm -rf "$directory"
done

if [ 'true' = "$tests_failed" ]; then
    error 'One or more integration tests failed'
fi
