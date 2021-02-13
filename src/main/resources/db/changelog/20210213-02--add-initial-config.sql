--liquibase formatted sql

--changeset amrw:20210213-02
INSERT INTO CONFIG (NAME, VALUE) VALUES
    ('DISCORD_AUTH_TOKEN', ''),
    ('EMBED_COLOUR', 'orange'),
    ('TRIGGER', '!gg');

--rollback DELETE FROM CONFIG WHERE NAME IN (
--rollback     'DISCORD_AUTH_TOKEN',
--rollback     'EMBED_COLOUR',
--rollback     'TRIGGER'
--rollback );
