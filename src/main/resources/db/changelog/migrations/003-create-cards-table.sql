--liquibase formatted sql
--changeset luis:003
--comment: cards table create

CREATE TABLE CARDS (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    board_columns_id BIGINT NOT NULL,

    CONSTRAINT boards_columns_cards_fk
        FOREIGN KEY (board_columns_id)
        REFERENCES BOARDS_COLUMNS(id)
        ON DELETE CASCADE
);

--rollback DROP TABLE CARDS;