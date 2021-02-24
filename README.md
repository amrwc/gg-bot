# GG Bot

[![Docker][github_badge_docker]][github_actions]
[![Unit and Integration Tests][github_badge_unit_integration]][github_actions]

Discord bot to play games with.

The list of available games can be viewed in [the
documentation][supported_games], or in the server where the bot has been added
using the `games` command.

## Documentation

The bot interacts with Discord using the [Javacord][javacord] library. The
documentation can be found [here][javacord_docs].

If the Javacord's documentation doesn't cover certain Discord-specific topics,
documentation of [Discord.js][discordjs_docs] may have more information.

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

### Migrations

See the [Database Migrations][db_migrations] document.

### Scripts

#### Install dependencies

```console
pip install -r bin/management/requirements.txt
```

#### Envars

Some scripts inside `bin/` require environment variables set. To unset them,
use this one-liner:

```bash
for var in $(export | grep -E '(POSTGRES|SPRING)' | awk -F'=' '{print $1}'); do unset "$var"; done
```

Note that in the above code snippet, `grep`'s use of `-E` flag may not work
outside of macOS.

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
[db_migrations]: ./docs/database-migrations.md
[discordjs_docs]: https://discordjs.guide
[github_actions]: https://github.com/amrwc/gg-bot/actions
[github_badge_docker]:
  https://github.com/amrwc/gg-bot/workflows/Docker/badge.svg
[github_badge_unit_integration]:
  https://github.com/amrwc/gg-bot/workflows/Unit%20and%20Integration%20Tests/badge.svg
[javacord]: https://github.com/Javacord/Javacord
[javacord_docs]: https://javacord.org/wiki
[supported_games]: ./docs/supported-games.md
