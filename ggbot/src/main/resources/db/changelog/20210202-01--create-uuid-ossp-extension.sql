--liquibase formatted sql

--changeset amrw:20210202-01
CREATE EXTENSION "uuid-ossp";

--rollback DROP EXTENSION "uuid-ossp";
