# Spring Boot Template

[![Docker](https://github.com/amrwc/spring-boot-template/workflows/Docker/badge.svg)](https://github.com/amrwc/spring-boot-template/actions)
[![Unit and Integration Tests](https://github.com/amrwc/spring-boot-template/workflows/Unit%20and%20Integration%20Tests/badge.svg)](https://github.com/amrwc/spring-boot-template/actions)

In this template:

- Spring Boot,
- PostgreSQL,
- Docker + Docker Compose,
- GitHub workflows:
  - pure Docker and Docker Compose setups,
  - unit and integration tests of the Spring Boot application.

This template has been bootstrapped using [this Spring Initializr
configuration][spring_initializr].

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

## API

### `/api/welcome`

#### GET `[/<id>]`

Returns a welcome message with the given ID.

Path variables:

- `id` – primary key of the `WELCOME_MESSAGES` table. Default: `1`

##### Example

```console
# {"id":2,"content":"Foo"}
curl http://localhost:8080/api/welcome/2
```

#### POST

Stores a new welcome message in the database.

Request body:

- `content` – welcome message content.

##### Example

```console
curl \
    --request POST \
    --header 'Content-Type: application/json' \
    --data '{"content": "Bar"}' \
    http://localhost:8080/api/welcome
```

## Caveats

- The `--suspend` option in `setup.sh` doesn't seem to work – something's wrong
  with the `8000` port when the application is suspended, and the debugger
  fails to connect.

## White-label clean-up

Places around the project that need renaming.

<details>

<summary>
Click here to expand
</summary>

1. `.github/workflows/docker.yml`:
   - `MAIN_IMAGE: 'renameme'`
   - `url='http://localhost:8080/api/welcome/1'`
1. `docker/docker-compose.yml`:
   - `renameme-network:`
   - `container_name: renameme-database`
   - `- renameme-network`
   - `renameme-service`
   - `container_name: renameme`
   - `image: renameme`
   - `- ${GRADLE_IMAGE:-renameme-gradle_container}`
   - `- ${MAIN_IMAGE:-renameme}`
   - `- renameme-database`
1. `docker/postgres-envars.list`:
   - `POSTGRES_DB=renameme`
1. `bin/pgadmin.sh`:
   - `MAIN_IMAGE='renameme'`
1. `bin/integration_tests.sh`:
   - `MAIN_IMAGE='renameme'`
1. `bin/setup.sh`:
   - `MAIN_IMAGE='renameme'`
1. `bin/start.sh`:
   - `MAIN_IMAGE='renameme'`
1. `bin/teardown.sh`:
   - `MAIN_IMAGE='renameme'`
1. Directory structure:
   - `src/main/java/me/rename/renameme`
   - `src/test/java/me/rename/renameme`
1. `src/main/resources/application.yml`:
   - `url: 'jdbc:postgresql://renameme-database:5432/renameme'`
1. `src/main/resources/liquibase.properties`:
   - `url=jdbc:postgresql://localhost:5432/renameme`
1. `src/main/resourcees/log4j2.xml`:
   - `fileName="log/renameme.log"`
   - `filePattern="log/renameme-%d{yyyy-MM-dd}-%i.log.gz"`
   - `<IfFileName glob="log/renameme-*.log.gz"/>`
1. `src/test/resources/application.yml`:
   - `url: 'jdbc:postgresql://127.0.0.1:5432/renameme'`
1. `src/test/resources/log4j2.xml`:
   - `fileName="log/test/renameme.log"`
   - `filePattern="log/test/renameme-%d{yyyy-MM-dd}-%i.log.gz"`
   - `<IfFileName glob="log/test/renameme-*.log.gz"/>`
1. `build.gradle`:
   - `group: 'me.rename'`
1. `settings.gradle`:
   - `rootProject.name = 'renameme'`

</details>

[spring_initializr]:
  https://start.spring.io/#!type=gradle-project&language=java&platformVersion=2.4.2.RELEASE&packaging=jar&jvmVersion=11&groupId=me.rename&artifactId=renameme&name=renameme&description=&packageName=me.rename.renameme&dependencies=devtools,lombok,web,data-jpa,liquibase,postgresql,validation
[db_migrations]: ./docs/database-migrations.md
