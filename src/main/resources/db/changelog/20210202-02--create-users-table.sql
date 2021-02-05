--liquibase formatted sql

--changeset amrw:20210202-02
CREATE TABLE USERS (
    ID               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    DISCORD_USER_ID  VARCHAR(20) NOT NULL CHECK (DISCORD_USER_ID <> ''),
    DISCORD_USERNAME TEXT NOT NULL CHECK (DISCORD_USERNAME <> '')
);
CREATE UNIQUE INDEX USERS__DISCORD_USER_ID_INDEX ON USERS(DISCORD_USER_ID);
COMMENT ON TABLE USERS IS 'Users that have interacted with the bot';
COMMENT ON COLUMN USERS.ID IS 'Unique identifier of a user';
COMMENT ON COLUMN USERS.DISCORD_USER_ID IS 'An ID of a Discord user';
COMMENT ON COLUMN USERS.DISCORD_USERNAME IS 'Username of the given Discord user; can change over time';

--rollback DROP TABLE USERS;
