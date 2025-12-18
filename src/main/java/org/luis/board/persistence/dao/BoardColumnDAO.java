package org.luis.board.persistence.dao;

import org.luis.board.dto.BoardColumnDTO;
import org.luis.board.persistence.entity.BoardColumnEntity;
import org.luis.board.persistence.entity.CardEntity;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.luis.board.persistence.entity.BoardColumnKindEnum.findByName;
import static java.util.Objects.isNull;


@RequiredArgsConstructor
public class BoardColumnDAO {

    private final Connection connection;

    public BoardColumnEntity insert(final BoardColumnEntity entity) throws SQLException {
        var sql = """
        INSERT INTO BOARDS_COLUMNS (name, "order", kind, board_id)
        VALUES (?, ?, ?, ?)
        """;

        try (var statement = connection.prepareStatement(
                sql,
                java.sql.Statement.RETURN_GENERATED_KEYS
        )) {
            var i = 1;
            statement.setString(i++, entity.getName());
            statement.setInt(i++, entity.getOrder());
            statement.setString(i++, entity.getKind().name());
            statement.setLong(i, entity.getBoard().getId());
            statement.executeUpdate();

            try (var rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    entity.setId(rs.getLong(1));
                }
            }
            return entity;
        }
    }


    public List<BoardColumnEntity> findByBoardId(final Long boardId) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();

        var sql = """
        SELECT id, name, "order", kind
          FROM BOARDS_COLUMNS
         WHERE board_id = ?
         ORDER BY "order"
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    var entity = new BoardColumnEntity();
                    entity.setId(rs.getLong("id"));
                    entity.setName(rs.getString("name"));
                    entity.setOrder(rs.getInt("order"));
                    entity.setKind(findByName(rs.getString("kind")));
                    entities.add(entity);
                }
            }
        }
        return entities;
    }


    public List<BoardColumnDTO> findByBoardIdWithDetails(final Long boardId) throws SQLException {
        List<BoardColumnDTO> dtos = new ArrayList<>();

        var sql = """
        SELECT bc.id        AS id,
               bc.name      AS name,
               bc.kind      AS kind,
               (
                   SELECT COUNT(c.id)
                     FROM CARDS c
                    WHERE c.board_column_id = bc.id
               ) AS cards_amount
          FROM BOARDS_COLUMNS bc
         WHERE bc.board_id = ?
         ORDER BY bc."order";
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    var dto = new BoardColumnDTO(
                            rs.getLong("id"),
                            rs.getString("name"),
                            findByName(rs.getString("kind")),
                            rs.getInt("cards_amount")
                    );
                    dtos.add(dto);
                }
            }
        }
        return dtos;
    }


    public Optional<BoardColumnEntity> findById(final Long columnId) throws SQLException {
        var sql = """
        SELECT bc.name      AS column_name,
               bc.kind      AS column_kind,
               c.id         AS card_id,
               c.title      AS card_title,
               c.description AS card_description
          FROM BOARDS_COLUMNS bc
          LEFT JOIN CARDS c
            ON c.board_column_id = bc.id
         WHERE bc.id = ?;
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, columnId);
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                var entity = new BoardColumnEntity();
                entity.setName(rs.getString("column_name"));
                entity.setKind(findByName(rs.getString("column_kind")));

                do {
                    if (rs.getObject("card_id") == null) {
                        break;
                    }
                    var card = new CardEntity();
                    card.setId(rs.getLong("card_id"));
                    card.setTitle(rs.getString("card_title"));
                    card.setDescription(rs.getString("card_description"));
                    entity.getCards().add(card);
                } while (rs.next());

                return Optional.of(entity);
            }
        }
    }


}