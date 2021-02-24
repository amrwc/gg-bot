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

Java library for interacting with Discord servers, and therefore developing
bots. It receives events from Discord websocket, and passes them in a
formalised format to listeners of corresponding type.

The event objects contain information that can be used for responding to the
events on a Discord server. For instance, `MessageCreatedEvent` contains a
`TextChannel` object, which can be directly used to send a message to the
channel.

### Listeners

Handlers for events coming from Javacord. Each listener class directly, or
indirectly, implements some `Listener` interface, and is therefore picked up by
Javacord which passes down events for processing. They parse/translate
information from text messages into POJOs, which are then passed down to
Services for further processing.

### Services

They implement logic behind games and transactions, and pass the results to the
database via Repositories.

### Repositories

Persistence layer for communicating with the database.

### Database

PostgreSQL database for storing user credits, game outcomes, and some other
miscellaneous information. Each Discord user has a unique ID, therefore
identifying users with that ID is trivial, and persisting their scores and
credits is easily done.

## Docker

```text
                                +----------------------------+
                                |      Docker Network        |
                                |                            |
+----------------------+        |  +----------------------+  |
|                      |    JAR |  |                      |  |
|   Build Container    | <------+> |    Main Container    |  |
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

Container for building and packaging the Spring Boot application using Gradle.

### Build cache volume

Volume for storing Gradle build cache. By using it, each subsequent build is
faster than the initial one, even if the build container is destroyed.

### Main container

Container for running the Spring Boot application. It's enclosed in a Docker
network along with the database container.

### Database container

Container running the PostgreSQL server.
