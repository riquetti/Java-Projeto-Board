package org.luis.board.persistence.entity;

import lombok.Data;
import lombok.Setter;

@Data
public class CardEntity {

    private Long id;
    private String title;
    private String description;
    private BoardColumnEntity boardColumn = new BoardColumnEntity();

    @Setter
    private CardStatus status;

    public CardEntity(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = CardStatus.TO_DO; // status inicial
    }

    public CardEntity() {

    }
}
