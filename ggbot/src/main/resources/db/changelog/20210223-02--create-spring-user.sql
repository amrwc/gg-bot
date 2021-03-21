--liquibase formatted sql

--changeset amrw:20210223-02
CREATE USER spring_user WITH PASSWORD 'SpringUserPassword';
GRANT readwrite TO spring_user;

--rollback REVOKE readwrite FROM spring_user;
--rollback DROP USER spring_user;
