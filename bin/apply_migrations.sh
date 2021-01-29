#!/usr/bin/env sh

DRIVER_URL='https://jdbc.postgresql.org/download/postgresql-42.2.18.jar'
SHA256_DRIVER='0c891979f1eb2fe44432da114d09760b5063dad9e669ac0ac6b0b6bfb91bb3ba'
DRIVER_PATH='./tmp/db-driver/postgresql.jar'

LIQUIBASE_URL='https://github.com/liquibase/liquibase/releases/download/v4.2.2/liquibase-4.2.2.tar.gz'
SHA256_LIQUIBASE_ARCHIVE='807ef4b514d01fc62f7aaf4150a8435c90ccb5986f3272d3cfd1bd26c2cf7b4c'
LIQUIBASE_DIR='./tmp/liquibase'
LIQUIBASE_PATH="${LIQUIBASE_DIR}/liquibase.jar"
LIQUIBASE_ARCHIVE="${LIQUIBASE_DIR}/liquibase.tar.gz"

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

if [ ! -f "$DRIVER_PATH" ]; then
    log "Downloading database driver to '${DRIVER_PATH}'"
    mkdir -p "$(dirname "$DRIVER_PATH")"
    curl --silent --output "${DRIVER_PATH}" "$DRIVER_URL"
    sha256="$(sha256sum "$DRIVER_PATH" | awk '{printf $1}')"
    if [ "$SHA256_DRIVER" != "$sha256" ]; then
        error "SHA256 checksum of '${DRIVER_PATH}' doesn't match '${SHA256_DRIVER}'"
    fi
fi

liquibase_cmd='liquibase'
if ! $liquibase_cmd --version >/dev/null 2>&1; then
    if [ ! -f "$LIQUIBASE_PATH" ]; then
        log "Downloading and extracting Liquibase to '${LIQUIBASE_PATH}'"
        mkdir -p "$LIQUIBASE_DIR"
        curl --silent --location --output "$LIQUIBASE_ARCHIVE" "$LIQUIBASE_URL"
        sha256="$(sha256sum "$LIQUIBASE_ARCHIVE" | awk '{printf $1}')"
        if [ "$SHA256_LIQUIBASE_ARCHIVE" != "$sha256" ]; then
            error "SHA256 checksum of '${LIQUIBASE_ARCHIVE}' doesn't match '${SHA256_LIQUIBASE_ARCHIVE}'"
        fi
        tar -C "$LIQUIBASE_DIR" -zxf "$LIQUIBASE_ARCHIVE" liquibase.jar
        rm "$LIQUIBASE_ARCHIVE"
    fi
    liquibase_cmd="java -jar ${LIQUIBASE_PATH}"
fi

# shellcheck disable=SC2086
eval $liquibase_cmd \
    --defaultsFile='src/main/resources/liquibase.properties' \
    --classpath="${DRIVER_PATH}" \
    update
