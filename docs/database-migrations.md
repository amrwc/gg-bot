# Database Migrations

## Change sets

Migrations are done with [Liquibase][liquibase_docs] using [SQL
format][liquibase_sql_format]. The SQL change sets are located in
`src/main/resources/db/changelog`.

Each change set, to be applied, must be listed in the
`src/main/resources/db/changelog-main.xml` file inside the
`<databaseChangeLog>` tag. The items in the file are in the following format:

```xml
<databaseChangeLog>
    <include relativeToChangelogFile="true" file="changelog/<YYYYMMDD>-<NN>--<migration_name>.sql"/>
</databaseChangeLog>
```

Example:

```xml
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
         http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd"
>
    <include relativeToChangelogFile="true" file="changelog/20210113-01--create-users-table.sql"/>
</databaseChangeLog>
```

More information can be found in
[Liquibase documentation][liquibase_sql_format].

## Applying migrations

### Liquibase executable and Postgres driver

`database.py` checks for a local Liquibase installation, and if it's not
present, it downloads the `liquibase.tar.gz` archive and extracts
`liquibase.jar` into `./tmp/liquibase` directory to use for migrations.

Similarly, the PostgreSQL driver is downloaded into `./tmp/db-driver` if it's
not already there.

Both files are checked for a SHA256 digest match, and the script fails if the
downloaded files don't match. In case of an upgrade, update the hashes inside
`database.py`. Use the following snippet to obtain the digest:

```console
sha256sum ./tmp/db-driver/postgresql.jar | awk '{print $1}'
```

### Using `./bin/database.py`

```console
export POSTGRES_URL='jdbc:postgresql://localhost:5432'
export POSTGRES_DB='dbname'
export POSTGRES_USER='postgres'
export POSTGRES_PASSWORD='SuperuserPassword'
./bin/database.py --apply-migrations

# Using Liquibase directly
liquibase \
    --classpath=./tmp/db-driver/postgresql.jar \
    --defaultsFile=src/main/resources/liquibase.properties \
    --url=jdbc:postgresql://localhost:5432/dbname \
    --username=postgres \
    --password=SuperuserPassword \
    update
```

## Rolling back

```console
./bin/database.py --rollback 1

# Using Liquibase directly
liquibase \
    --classpath=./tmp/db-driver/postgresql.jar \
    --defaultsFile=src/main/resources/liquibase.properties \
    --url=jdbc:postgresql://localhost:5432/dbname \
    --username=postgres \
    --password=SuperuserPassword \
    rollbackCount 1
```

## Applying migrations automatically with Spring

To let Spring apply the migrations on startup, import Liquibase inside
`build.gradle`.

```groovy
implementation "org.liquibase:liquibase-core:4.2.2"
```

In `application.yml`, set the following:

```yml
spring:
  datasource:
    url: 'jdbc:postgresql://localhost:5432/dbname'
    username: 'springuser'
    password: 'SpringUserPassword'
  liquibase:
    change-log: 'classpath:db/changelog-main.xml'
  jpa:
    hibernate:
      ddl-auto: 'update'
    # See: https://stackoverflow.com/a/48222934/10620237
    open-in-view: false
```

## Caveats

- If the initial migrations are applied by Spring, doing rollbacks on the
  database using local Liquibase installation doesn't seem to work.
  - A solution to this is simply not allow Spring to apply the migrations on
    startup and do it manually, which is also a better practice in general.
  - Although, a solution to the problem at hand is to run `update` and then
    it's possible to roll back locally. Though, it negates the initial
    migration by Spring, making it redundant in the first place.

[liquibase_docs]: https://docs.liquibase.com
[liquibase_download]: https://www.liquibase.org/download
[liquibase_sql_format]:
  https://docs.liquibase.com/concepts/basic/sql-format.html
[postgres_download]: https://jdbc.postgresql.org/download.html
