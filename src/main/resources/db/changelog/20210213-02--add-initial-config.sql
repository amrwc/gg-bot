--liquibase formatted sql

--changeset amrw:20210213-02
INSERT INTO CONFIG (NAME, VALUE) VALUES
    ('DISCORD_API_KEY', ''),
    ('EMBED_COLOUR', 'orange'),
    ('TRIGGER', '!gg');

--rollback DELETE FROM CONFIG WHERE NAME IN (
--rollback     'DISCORD_API_KEY',
--rollback     'EMBED_COLOUR',
--rollback     'TRIGGER'
--rollback );
