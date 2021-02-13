--liquibase formatted sql

--changeset amrw:20210213-01
CREATE TABLE CONFIG (
    ID    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    NAME  VARCHAR NOT NULL CHECK (NAME <> ''),
    VALUE TEXT NOT NULL
);
CREATE UNIQUE INDEX CONFIG__NAME_INDEX ON CONFIG(NAME);
COMMENT ON TABLE CONFIG IS 'Application configuration';
COMMENT ON COLUMN CONFIG.ID IS 'Unique identifier of a configuration property';
COMMENT ON COLUMN CONFIG.NAME IS 'Name (key) of a configuration property';
COMMENT ON COLUMN CONFIG.VALUE IS 'Stringified value of a configuration property to be casted to appropriate type';

--rollback DROP TABLE CONFIG;
