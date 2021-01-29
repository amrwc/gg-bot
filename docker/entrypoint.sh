#!/usr/bin/env sh

DEBUG_PORT='8000'

# shellcheck disable=SC2153
if [ 'true' = "$DEBUG" ] || [ 'true' = "$SUSPEND" ]; then
    [ 'true' = "$SUSPEND" ] && suspend='y' || suspend='n'
    debug_arg="-agentlib:jdwp=transport=dt_socket,server=y,suspend=${suspend},address=*:${DEBUG_PORT}"
fi

# shellcheck disable=SC2086
java $debug_arg -jar app.jar
