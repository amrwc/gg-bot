# GG Bot

[![Docker](https://github.com/amrwc/gg-bot/workflows/Docker/badge.svg)](https://github.com/amrwc/gg-bot/actions)
[![Unit and Integration Tests](https://github.com/amrwc/gg-bot/workflows/Unit%20and%20Integration%20Tests/badge.svg)](https://github.com/amrwc/gg-bot/actions)

Discord bot to play games with.

The list of available games can be viewed in
[the documentation][supported_games], or in the server where the bot has been
added using the `games` command.

## Documentation

The bot interacts with Discord using the [Javacord][javacord] library. The
documentation can be found [here][javacord_docs].

If the Javacord's documentation doesn't cover certain Discord-specific topics,
documentation of [Discord.js][discordjs_docs] may have more information.

## Setup

### Discord

1. Create a new application on
   [Discord Developer Portal](https://discord.com/developers/applications).
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
       navigate to it in the browser. Sign in to a relevant Discord account and
       add the bot to a server.
1. Create bot config.
  1. Duplicate `bot-config.example.yml` inside `src/main/resources/` and call
     it `bot-config.yml`.
  1. Replace the example the authentication token in the config file with the
     one generated on Discord Developer Portal.

### Migrations

See the [Database Migrations][db_migrations] document.

### Docker

```console
./bin/run.sh [--apply-migrations --cache-from <cache_image_tag> --debug --detach --no-cache --start-db --suspend]
```

The application is now listening at `http://localhost:8080`. If the `--debug`
option has been used, the debugger is listening on port `8000`.

#### Clean

```console
./bin/teardown.sh [--include-cache --include-db --include-tmp]
```

## Test

### Unit tests

```console
./gradlew test
```

### Integration tests

```console
./bin/integration_tests.sh
```

[db_migrations]: ./docs/database-migrations.md
[discordjs_docs]: https://discordjs.guide
[javacord]: https://github.com/Javacord/Javacord
[javacord_docs]: https://javacord.org/wiki
[supported_games]: ./docs/supported-games.md
