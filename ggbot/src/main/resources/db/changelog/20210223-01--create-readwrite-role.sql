--liquibase formatted sql

--changeset amrw:20210223-01
-- See: https://aws.amazon.com/blogs/database/managing-postgresql-users-and-roles

-- Prevent inheritance of default permissions from `public` schema
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON DATABASE ggbot FROM PUBLIC;

CREATE ROLE readwrite;

-- Grant this role permission to connect to the database
GRANT CONNECT ON DATABASE ggbot TO readwrite;

-- Grant schema usage privilege
GRANT USAGE ON SCHEMA public TO readwrite;

-- Grant access to the tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO readwrite;

-- Automatically grant permissions on tables and views added in the future
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO readwrite;

-- Grant usage permission to all sequences, if any
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO readwrite;

-- Grant usage permission to all sequences automatically by default
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE ON SEQUENCES TO readwrite;

--rollback ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE USAGE ON SEQUENCES from readwrite;
--rollback REVOKE USAGE ON ALL SEQUENCES IN SCHEMA public FROM readwrite;
--rollback ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE SELECT, INSERT, UPDATE, DELETE ON TABLES FROM readwrite;
--rollback REVOKE SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public FROM readwrite;
--rollback REVOKE USAGE ON SCHEMA public FROM readwrite;
--rollback REVOKE CONNECT ON DATABASE ggbot FROM readwrite;

--rollback DROP ROLE readwrite;

--rollback GRANT ALL ON DATABASE ggbot TO PUBLIC;
--rollback GRANT CREATE ON SCHEMA public TO PUBLIC;
