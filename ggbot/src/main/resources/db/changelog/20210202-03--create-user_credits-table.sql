--liquibase formatted sql

--changeset amrw:20210202-03
CREATE TABLE USER_CREDITS (
    ID         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    USER_ID    UUID NOT NULL REFERENCES USERS(ID) ON DELETE CASCADE,
    CREDITS    INTEGER NOT NULL CHECK (CREDITS >= 0),
    LAST_DAILY TIMESTAMP
);
CREATE UNIQUE INDEX USER_CREDITS__USER_ID_INDEX ON USER_CREDITS(USER_ID);
COMMENT ON TABLE USER_CREDITS IS 'User credits-related data';
COMMENT ON COLUMN USER_CREDITS.ID IS 'Unique identifier of user credits';
COMMENT ON COLUMN USER_CREDITS.USER_ID IS 'Unique identifier of a user';
COMMENT ON COLUMN USER_CREDITS.CREDITS IS 'Amount of credits available to the given user';
COMMENT ON COLUMN USER_CREDITS.LAST_DAILY IS 'When the daily credits were successfully claimed last time.';

--rollback DROP TABLE USER_CREDITS;
