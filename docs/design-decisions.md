# Design Decisions

Certain highlights of design decisions made over time, roughly in a
chronological order.

## Separate Dockerfile for build container

The initial Dockerfile used a multi-stage setup, but it proved quite bad at
reusing cache – on every run, Gradle cache was rebuilt, which resulted in build
times taking over 1m30s. Using the `--cache-from <stage_alias>` Docker CLI
option didn't help; it reused layers in the first stage, but still ran both
stages right after anyway. Therefore, the advantages of having a single
Dockerfile and being able to copy the JAR file from the first stage were
quickly diminished by the performance of the setup.

Another downside of multi-stage build was the difficulty of caching the build
image in a CI pipeline. By splitting the images, they can easily be both tagged
and pushed to a container registry for reusing them in subsequent runs.

Having separate build image allows for maintaining Gradle cache in a Docker
volume, and still caching all the layers of the build Dockerfile. Build times
take around 20s in best cases using this approach.

The disadvantages are: having to maintain two Dockerfiles, and having to copy
the packaged application via the host machine instead of directly between
containers as `docker cp` doesn't support such action.

## Removing Docker Compose support

Due to the change from multi-stage build to separate Dockerfiles, it's not
possible to copy the JAR file between containers within Docker Compose context.

## Keeping Discord API key inside the database

Initially, the application read a config file from the classpath. Since the
`BotConfig` bean relied on the file to exist, the application failed to start
if it was missing. To mitigate that, the bean factory had the
`@ConditionalOnResource(resources = {BotConfig.PATH})` annotation to only
attempt the creation if the file existed. This, however, caused a chain
reaction with the `DiscordApi` bean as it requires an API key to start
successfully. Therefore, since the respective bean factory is annotated with
`@ConditionalOnBean(BotConfig.class)` and the `BotConfig` bean was conditional
on the config file, `DiscordApi` bean couldn't be produced.

By moving the config properties into the `CONFIG` table in the database, the
`BotConfig` bean can be easily created, and `DiscordApi` bean right after.

### Possible improvements

- Use Vault for storing secrets instead of having the unencrypted values in the
  database tainted in case it gets compromised.
- Using config files isn't the worst idea – it can be handled pretty well by
  Spring Boot with the `<file_name>-<profile_name>.yml` naming format.
  Although, it's best to have the deployment process automated in some safer
  fashion rather than relying on `.gitignore` and keeping the plain-text
  secrets in a local file.

## Passing secrets as environment variables

It's easy to pass and reuse secrets exported in the current shell session
instead of passing them into the script as command-line arguments. I'd argue
that it's even more secure as previous executions of a script can be found in
shell history.

Relying on `export` for security is not ideal either, but it simplifies usage
of said scripts as otherwise, to do it in a more secure fashion, the user would
have to do something like this:

```console
bin/run.py --db-password "$(echo -n "$POSTGRES_PASSWORD")"
```

The above is cumbersome, more error-prone, and still relies on local variables.

## Hard-coding database user in the migration

Naturally, the hard-coded value is not meant to be used in any sort of real
environment. Instead, it's there for convenience when building the full
application in a CI pipeline, or locally for integration and API tests. It can,
and should be changed when the application is deployed.

### Possible improvements

- It can easily be automated with a Python package for interacting with
  Postgres databases. Simply use the superuser credentials, and run a basic
  query with the new password passed into the script in an environment
  variable.

## Revoking default permissions on the `public` schema

The approach to revoke default permissions of the default `public` role follows
the Amazon's recommendation found here:
<https://aws.amazon.com/blogs/database/managing-postgresql-users-and-roles>

> When a new database is created, PostgreSQL by default creates a schema named
> public and grants access on this schema to a backend role named public. All
> new users and roles are by default granted this public role, and therefore
> can create objects in the public schema. (...) By default, all users have
> access to create objects in the public schema.

Therefore, revoking the default `CREATE` permission on the `public` schema
allows for more granular permissions for roles.
