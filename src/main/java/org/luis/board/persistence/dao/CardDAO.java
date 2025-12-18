package org.luis.board.persistence.dao;

import org.luis.board.dto.CardDetailsDTO;
import org.luis.board.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class CardDAO {

    private final Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        var sql = """
            INSERT INTO CARDS (title, description, board_column_id)
            VALUES (?, ?, ?);
            """;

        try (var statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {
            var i = 1;
            statement.setString(i++, entity.getTitle());
            statement.setString(i++, entity.getDescription());
            statement.setLong(i++, entity.getBoardColumn().getId());
            statement.executeUpdate();

            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getLong(1));
                }
            }
        }
        return entity;
    }

    public void moveToColumn(final Long columnId, final Long cardId) throws SQLException {
        var sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?;";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setLong(i++, columnId);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }

    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        var sql = """
            SELECT c.id                AS card_id,
                   c.title             AS card_title,
                   c.description       AS card_description,
                   b.blocked_at         AS blocked_at,
                   b.block_reason       AS block_reason,
                   c.board_column_id   AS column_id,
                   bc.name              AS column_name,
                   (
                       SELECT COUNT(sub_b.id)
                         FROM BLOCKS sub_b
                        WHERE sub_b.card_id = c.id
                   ) AS blocks_amount
              FROM CARDS c
              LEFT JOIN BLOCKS b
                ON c.id = b.card_id
               AND b.unblocked_at IS NULL
             INNER JOIN BOARDS_COLUMNS bc
                ON bc.id = c.board_column_id
             WHERE c.id = ?;
            """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    var blockedAt = rs.getTimestamp("blocked_at");

                    var dto = new CardDetailsDTO(
                            rs.getLong("card_id"),
                            rs.getString("card_title"),
                            rs.getString("card_description"),
                            nonNull(rs.getString("block_reason")),
                            nonNull(blockedAt) ? toOffsetDateTime(blockedAt) : null,
                            rs.getString("block_reason"),
                            rs.getInt("blocks_amount"),
                            rs.getLong("column_id"),
                            rs.getString("column_name")
                    );
                    return Optional.of(dto);
                }
            }
        }
        return Optional.empty();
    }
}
