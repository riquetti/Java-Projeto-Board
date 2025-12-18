--liquibase formatted sql
--changeset luis:001
--comment: boards table create

CREATE TABLE BOARDS (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

--rollback DROP TABLE boards;
