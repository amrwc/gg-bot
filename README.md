# GG Bot

[![Docker](https://github.com/amrwc/gg-bot/workflows/Docker/badge.svg)](https://github.com/amrwc/gg-bot/actions)
[![Unit and Integration Tests](https://github.com/amrwc/gg-bot/workflows/Unit%20and%20Integration%20Tests/badge.svg)](https://github.com/amrwc/gg-bot/actions)

Discord bot to play games with.

## Setup

### Migrations

See the [Database Migrations][db_migrations] document.

### `docker run`

```console
./bin/setup.sh [(--debug|--suspend) --no-cache]
./bin/start.sh [--apply-migrations --detach --dont-stop-db]
```

The application is now listening at `http://localhost:8080`. If the `--debug`
option has been used, the debugger is listening on port `8000` as should be
confirmed in the logs.

#### Clean

```console
./bin/teardown.sh [--exclude-db --include-cache]
```

### `docker-compose`

```console
docker-compose --file docker/docker-compose.yml up --build
./bin/apply_migrations.sh
```

The application is now listening at `http://localhost:8080`.

#### Debug

```console
docker-compose --file docker/docker-compose.yml build --build-arg debug=true [--build-arg suspend=true]
docker-compose --file docker/docker-compose.yml up [--detach]
```

The debug port can be accessed at `http://localhost:8000`.

#### Clean

```console
docker-compose --file docker/docker-compose.yml down
```

### Gradle cache

To speed up the builds, reuse Gradle cache between runs.

It's included in the `setup.sh` script, but not when running `docker-compose`.

```console
docker volume create --name gradle-cache
```

Prune the cache:

```console
docker volume rm gradle-cache
```

## Test

### Unit tests

```console
./gradlew build && ./gradlew test
```

### Integration tests

```console
./gradlew build && ./bin/integration_tests.sh
```

## Caveats

- The `--suspend` option in `setup.sh` doesn't seem to work – something's wrong
  with the `8000` port when the application is suspended, and the debugger
  fails to connect.

[db_migrations]: ./docs/database-migrations.md
