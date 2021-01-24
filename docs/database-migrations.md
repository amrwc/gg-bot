# Database Migrations

## Prerequisites

- If only up/update operation is required, use the `./bin/apply_migrations.sh`
  script.
- Local Liquibase installation. Either download it [here][liquibase_download],
  or use package manager, such as Homebrew:

  ```console
  brew install liquibase
  ```

- Liquibase added to path to be accessible from anywhere. At the end of
  Homebrew installation, the following tip is displayed:

  ```console
  You should set the environment variable LIQUIBASE_HOME to
    /usr/local/opt/liquibase/libexec
  ```

  Therefore, do:

  ```console
  export LIQUIBASE_HOME='/usr/local/opt/liquibase/libexec'
  ```

- Database driver JAR. For Postgres, it can be downloaded
  [here][postgres_download].

## Change sets

Migrations are done with [Liquibase][liquibase_docs] using [SQL
format][liquibase_sql_format]. The SQL change sets are located in
`src/main/resources/db/changelog`.

Each change set, to be applied, must be listed in the
`src/main/resources/db/changelog-main.xml` file inside the
`<databaseChangeLog>` tag. The items in the file are in the following format:

```xml
<databaseChangeLog>
    <include relativeToChangelogFile="true" file="changelog/<YYYYMMDD>-<NN>-<migration_name>-.sql"/>
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
    <include relativeToChangelogFile="true" file="changelog/20210113-01--create-welcomemessages-table.sql"/>
</databaseChangeLog>
```

More information can be found in the documentation
[here][liquibase_sql_format].

## Applying migrations

Scripted:

```console
./bin/apply_migrations.sh
```

Manually:

```console
liquibase \
    --defaultsFile='src/main/resources/liquibase.properties' \
    --classpath='<db_driver_path>' \
    update
```

## Rolling back

```console
liquibase \
    --defaultsFile='src/main/resources/liquibase.properties' \
    --classpath='<db_driver_path>' \
    rollbackCount 1
```

## Letting Spring apply the migrations automatically

To let Spring apply the migrations on startup, import Liquibase inside
`build.gradle`. The auto-configuration should work out-of-the-box and use the
database connection string from `application.yml`.

```groovy
implementation "org.liquibase:liquibase-core:4.2.2"
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
