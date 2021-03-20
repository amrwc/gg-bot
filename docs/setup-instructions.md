# Setup Instructions

## Database credentials

Change `spring_user` database password:

```sql
ALTER USER spring_user WITH PASSWORD '<new_password>';
```

## Discord

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

## GitHub Workflows

Add `CONTAINER_REGISTRY_TOKEN` secret to the repository to be able to sign into
container registry. Read more on this in [GitHub's
documentation][github_auth_container_registry].

[discord_developer_portal]: https://discord.com/developers/applications
[github_auth_container_registry]:
  https://docs.github.com/en/packages/guides/migrating-to-github-container-registry-for-docker-images#authenticating-with-the-container-registry
