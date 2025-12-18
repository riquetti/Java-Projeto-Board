package org.luis.board.persistence.entity;

import java.util.stream.Stream;

public enum BoardColumnKindEnum {

    INITIAL, FINAL, CANCEL, PENDING;

    public static BoardColumnKindEnum findByName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tipo de coluna (kind) está vazio ou nulo no banco");
        }

        return Stream.of(values())
                .filter(b -> b.name().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tipo de coluna inválido no banco: '" + name + "'"
                        )
                );
    }


}