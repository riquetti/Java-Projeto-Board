--liquibase formatted sql
--changeset luis:002
--comment: boards_columns table create

CREATE TABLE BOARDS_COLUMNS (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    "order" INT NOT NULL,
    kind CHAR(7) NOT NULL,
    board_id BIGINT NOT NULL,

    CONSTRAINT boards_columns_board_fk
        FOREIGN KEY (board_id)
        REFERENCES BOARDS(id)
        ON DELETE CASCADE,

    CONSTRAINT boards_columns_board_order_uk
        UNIQUE (board_id, "order")
);

--rollback DROP TABLE boards_columns;