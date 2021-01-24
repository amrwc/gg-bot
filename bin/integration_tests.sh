#!/usr/bin/env sh

MAIN_IMAGE='renameme'
DATABASE="${MAIN_IMAGE}-test-database"
DATABASE_IMAGE='postgres:latest'

DATABASE_PORT='5432'

NOFORMAT='\033[0m' RED='\033[0;31m' PURPLE='\033[0;35m'

echo "${PURPLE}Creating '${DATABASE_IMAGE}' container, name: ${DATABASE}${NOFORMAT}"
docker run --detach \
    --publish "${DATABASE_PORT}:${DATABASE_PORT}" \
    --env-file ./docker/postgres-envars.list \
    --name "$DATABASE" \
    "$DATABASE_IMAGE"

echo "${PURPLE}Applying database migrations${NOFORMAT}"
sleep 3 # Wait for the database to come up
./bin/apply_migrations.sh

echo "${PURPLE}Running integration tests${NOFORMAT}"
if ! ./gradlew integrationTest; then
    echo "${RED}One or more integration tests failed${NOFORMAT}"
    exit 1
fi

echo "${PURPLE}Stopping '${DATABASE}' container${NOFORMAT}"
docker stop "$DATABASE"

echo "${PURPLE}Removing '${DATABASE}' container${NOFORMAT}"
docker container rm --volumes "$DATABASE"
