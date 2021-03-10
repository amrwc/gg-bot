# GG Bot

[![Docker][github_badge_docker]][github_actions]
[![Unit and Integration Tests][github_badge_unit_integration]][github_actions]

Discord bot to play games with.

## Documentation

- [Architecture](./docs/architecture.md)
- [Database Inspection](./docs/database-inspection.md)
- [Database Migrations](./docs/database-migrations.md)
- [Design Decisions](./docs/design-decisions.md)
- [Supported Games](./docs/supported-games.md)
- [Working with `bin` Scripts](./docs/working-with-bin-scripts.md)

## Setup

### Discord

1. Create a new application on [Discord Developer
   Portal][discord_developer_portal].
   1. `Bot` settings.
      1. Add a bot and customise it.
      1. Save the authentication token.
   1. `OAuth2` settings.
      1. Add a redirect at `http://localhost:5000`.
      1. Choose the newly added redirect URL in the `OAuth2 URL Generator`
         section.
      1. In `scopes` section, tick `bot`.
      1. In `bot permissions` section, tick `Administrator`, or choose more
         granular permissions.
      1. Copy the invitation URL from the bottom of the `scopes` section and
         navigate to it in the browser. Sign in to a relevant Discord account
         and add the bot to a server.
1. Once the application's database has been provisioned, update the
   `DISCORD_AUTH_TOKEN` row to the authentication token.

   ```postgresql
   UPDATE CONFIG SET VALUE = '<auth_token>' WHERE NAME = 'DISCORD_AUTH_TOKEN';
   ```

### GitHub Workflows

Add `CR_PAT` secret to the repository to be able to sign into container
registry. Read more on this in [GitHub's
documentation][github_auth_container_registry].

### Docker

```console
export SPRING_DATASOURCE_URL='jdbc:postgresql://database-container:5432/dbname'
export SPRING_DATASOURCE_USERNAME='spring_user'
export SPRING_DATASOURCE_PASSWORD='SpringUserPassword'

export POSTGRES_URL='jdbc:postgresql://localhost:5432'
export POSTGRES_DB='dbname'
export POSTGRES_USER='postgres'
export POSTGRES_PASSWORD='SuperuserPassword'

./bin/run.py --apply-migrations [--debug]
```

The application is now listening at `http://localhost:8080`. If the `--debug`
option has been used, the debugger is listening on port `8000`.

Defaults such as database and Spring ports, and volume, network, image,
container names can be adjusted inside `./bin/config.ini`.

#### Clean

```console
./bin/teardown.py [--cache --db --network --tmp]
```

### Change `spring_user` database password

```sql
ALTER USER spring_user WITH PASSWORD '<new_password>';
```

## Test

### Unit tests

```console
./gradlew test
```

### Integration tests

```console
./bin/integration_tests.py
```

[discord_developer_portal]: https://discord.com/developers/applications
[github_actions]: https://github.com/amrwc/gg-bot/actions
[github_badge_docker]:
  https://github.com/amrwc/gg-bot/workflows/Docker/badge.svg
[github_badge_unit_integration]:
  https://github.com/amrwc/gg-bot/workflows/Unit%20and%20Integration%20Tests/badge.svg
[github_auth_container_registry]:
  https://docs.github.com/en/packages/guides/migrating-to-github-container-registry-for-docker-images#authenticating-with-the-container-registry
