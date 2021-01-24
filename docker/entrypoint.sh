#!/usr/bin/env sh

# shellcheck disable=SC2153
[ 'true' = "$SUSPEND" ] && _suspend='y' || _suspend='n'
[ 'true' = "$DEBUG" ] \
    && _debug_arg="-agentlib:jdwp=transport=dt_socket,server=y,suspend=${_suspend},address=*:8000" \
    || _debug_arg=''

# shellcheck disable=SC2086
java $_debug_arg -jar app.jar
