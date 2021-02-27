# Architecture

## Spring Boot application

```text
+----------------+            +----------------+
|                |   Events   |                |
|    Javacord    | ---------> |   Listeners    |
|                | <--------- |                |
+----------------+  Messages  +----------------+
                                      ^
                                  DTOs|
                                      v
                              +----------------+
                              |                |
                              |    Services    |
                              |                |
                              +----------------+
                                      ^
                                Models|
                                      v
                              +----------------+            +----------------+
                              |                |  Entities  |                |
                              |  Repositories  | <--------> |    Database    |
                              |                |            |                |
                              +----------------+            +----------------+
```

### Javacord

[Javacord][javacord] is a Java library for interacting with Discord servers. It
receives events from Discord websocket, and passes them in a formalised format
to listeners of corresponding type.

The event objects contain information that can be used for responding to the
events on a Discord server. For instance, `MessageCreatedEvent` contains a
`TextChannel` object, which in turn exposes the `sendMessage()` method to send
a message to that channel.

### Listeners

Handlers for events coming from Javacord. Each listener class implements some
`Listener` interface directly, or indirectly, and is therefore picked up by
Javacord which passes events down for processing. They parse/translate the
information from text messages into POJOs (like parsing command-line
arguments), which are then passed down to Services for further processing.

### Services

They implement the logic behind games and transactions, and persist the results
in the database via Repositories.

### Repositories

Persistence layer for communicating with the database.

### Database

Relational database for storing user credits, game outcomes, and other
information. Each Discord user has a unique ID, therefore identifying users is
trivial, and so is maintaining their state (e.g. credits balance).

## Docker

```text
                                +----------------------------+
                                |       Docker Network       |
                                |                            |
+----------------------+        |  +----------------------+  |
|                      |    JAR |  |                      |  |
|   Build Container    | -------+> |    Main Container    |  |
|                      |        |  |                      |  |
+----------------------+        |  +----------------------+  |
           ^                    |             ^              |
     Gradle|cache               |     Entities|              |
           v                    |             v              |
+----------------------+        |  +----------------------+  |
|                      |        |  |                      |  |
|  Build Cache Volume  |        |  |  Database Container  |  |
|                      |        |  |                      |  |
+----------------------+        |  +----------------------+  |
                                |                            |
                                +----------------------------+
```

### Build container

Container for building and packaging the application.

### Build cache volume

Volume for storing build cache. It speeds up, each subsequent build, even if
the build container has been destroyed.

### Main container

Container for running the application. It's enclosed in a Docker network along
with the database container.

### Database container

Container running the relational database server.

[javacord]: https://github.com/Javacord/Javacord
