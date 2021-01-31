#!/usr/bin/env sh
set -e

URLS='http://localhost:8080/actuator/info'

for url in $URLS; do
    response_code="$(curl --silent --show-error --output /dev/null --write-out '%{http_code}' "$url")"
    if [ 200 -ne "$response_code" ]; then
      echo "::error::Expected 200 response code but received ${response_code} from ${url}"
    fi
done
