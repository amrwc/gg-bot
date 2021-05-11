# Design Decisions

Certain highlights of design decisions made over time, in a roughly
chronological order.

## Separate Dockerfile for build container

The initial Dockerfile used a multi-stage setup, but it proved to be quite bad
at reusing cache – on every run, Gradle cache was rebuilt, which resulted in
build (and rebuild) times taking over 1m30s. Using the
`--cache-from <stage_alias>` Docker CLI option didn't help; it reused layers in
the first stage, but still ran both stages right after anyway. Therefore, the
advantages of having a single Dockerfile, and being able to copy the JAR file
from the first stage were quickly diminished by the performance of the setup.

Another downside of a multi-stage build was the difficulty of caching the build
image in a CI pipeline. By splitting the images, they can easily be both tagged
and pushed to a container registry to be reused in subsequent runs.

Having separate build image allows for maintaining Gradle cache in a Docker
volume, and still caching all the layers of the build Dockerfile. Rebuilds take
around 20s in best cases using this approach.

The disadvantages:

- having to maintain two Dockerfiles, and
- having to copy the packaged application via the host machine instead of
  directly between containers as `docker cp` doesn't support such action.

## Removing Docker Compose support

Due to stepping away from multi-stage build to separate Dockerfiles, it's not
possible to copy the JAR file between containers within Docker Compose context.
To achieve that, the project's root directory would have to be mounted in the
container.

## Holding Discord API key in the database

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

- Use [Vault][hashicorp_vault] for storing secrets instead of storing the
  unencrypted values in the database, tainted in case it gets compromised.
- Using config files isn't the worst idea – it can be handled pretty well by
  Spring Boot with the `<file_name>-<profile_name>.yml` naming format.
  Although, it's best to have the deployment process automated in some safer
  fashion rather than relying on `.gitignore` and keeping the plain-text
  secrets in a local file.

## Passing secrets as environment variables

It's easy to pass and reuse secrets exported in the current shell session
instead of passing them into the script as command-line arguments. Although,
it's only slightly more secure as the `export` commands can be found in shell
history, just like previous script executions. It's not ideal, but it
simplifies usage of said scripts as otherwise, to do it in a more secure
fashion, the user would have to do something like this:

```console
bin/run.py --db-password "$(echo -n "$POSTGRES_PASSWORD")"
```

The above is cumbersome, more error-prone, and still relies on local variables.

### Possible improvements

- Use a config file where the secrets can be stored. This way, they can't be
  accessed through shell history, or by just running `export | grep POSTGRES`.
  Although, it has the same issues as storing the API keys locally (see above).

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
allows for more granular permission settings for specific roles.

## Rewriting Python `bin`/`runner` scripts in Java

The decision was to turn the bunch of hacked Python scripts into a
well-written, properly unit-tested, object-oriented set of command-line
applications.

[Picocli][picocli] turned out to be a perfect choice for bootstrapping a CLI
application -- it's simple to start with, and supports 'sub-commands', which
are essentially sub-applications.

[docker-java][docker_java] was used for interacting with Docker locally. It's
not beginner-friendly, and has basically no documentation on its own. The
maintainers emphasise that it's a thin, type-safe layer on top of the low-level
Docker API. Therefore, knowing the low-level Docker API is a prerequisite to
be productive with the framework.

[docker_java]: https://github.com/docker-java/docker-java
[hashicorp_vault]: https://www.vaultproject.io
[picocli]: https://picocli.info
