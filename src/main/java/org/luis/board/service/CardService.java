package org.luis.board.service;

import lombok.AllArgsConstructor;
import org.luis.board.dto.BoardColumnInfoDTO;
import org.luis.board.exception.CardBlockedException;
import org.luis.board.exception.CardFinishedException;
import org.luis.board.exception.EntityNotFoundException;
import org.luis.board.persistence.dao.BlockDAO;
import org.luis.board.persistence.dao.CardDAO;
import org.luis.board.persistence.entity.CardEntity;
import org.luis.board.persistence.entity.CardStatus;
import org.luis.board.persistence.entity.BoardColumnKindEnum;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    // =========================
    // STATUS DO CARD (DERIVADO)
    // =========================
    public CardStatus resolveStatus(
            Long columnId,
            List<BoardColumnInfoDTO> boardColumnsInfo
    ) {
        var currentColumn = boardColumnsInfo.stream()
                .filter(bc -> bc.id().equals(columnId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Coluna do card não encontrada"));

        if (currentColumn.kind() == BoardColumnKindEnum.CANCEL) {
            return CardStatus.CANCELLED;
        }

        if (currentColumn.kind() == BoardColumnKindEnum.FINAL) {
            return CardStatus.DONE;
        }

        if (currentColumn.order() == 0) {
            return CardStatus.TO_DO;
        }

        return CardStatus.IN_PROGRESS;
    }

    // =========================
    // CRUD / AÇÕES
    // =========================
    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }

    public void moveToNextColumn(
            final Long cardId,
            final List<BoardColumnInfoDTO> boardColumnsInfo
    ) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var dto = dao.findById(cardId).orElseThrow(
                    () -> new EntityNotFoundException(
                            "O card de id %s não foi encontrado".formatted(cardId))
            );

            if (dto.blocked()) {
                throw new CardBlockedException(
                        "O card %s está bloqueado, é necessário desbloqueá-lo para mover"
                                .formatted(cardId)
                );
            }

            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("O card informado pertence a outro board"));

            if (currentColumn.kind() == BoardColumnKindEnum.FINAL) {
                throw new CardFinishedException("O card já foi finalizado");
            }

            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("O card está cancelado"));

            dao.moveToColumn(nextColumn.id(), cardId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void cancel(
            final Long cardId,
            final Long cancelColumnId,
            final List<BoardColumnInfoDTO> boardColumnsInfo
    ) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var dto = dao.findById(cardId).orElseThrow(
                    () -> new EntityNotFoundException(
                            "O card de id %s não foi encontrado".formatted(cardId))
            );

            if (dto.blocked()) {
                throw new CardBlockedException(
                        "O card %s está bloqueado, é necessário desbloqueá-lo para mover"
                                .formatted(cardId)
                );
            }

            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("O card informado pertence a outro board"));

            if (currentColumn.kind() == BoardColumnKindEnum.FINAL) {
                throw new CardFinishedException("O card já foi finalizado");
            }

            dao.moveToColumn(cancelColumnId, cardId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void block(
            final Long id,
            final String reason,
            final List<BoardColumnInfoDTO> boardColumnsInfo
    ) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var dto = dao.findById(id).orElseThrow(
                    () -> new EntityNotFoundException(
                            "O card de id %s não foi encontrado".formatted(id))
            );

            if (dto.blocked()) {
                throw new CardBlockedException(
                        "O card %s já está bloqueado".formatted(id)
                );
            }

            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow();

            if (currentColumn.kind() == BoardColumnKindEnum.FINAL
                    || currentColumn.kind() == BoardColumnKindEnum.CANCEL) {
                throw new IllegalStateException(
                        "O card está em uma coluna do tipo %s e não pode ser bloqueado"
                                .formatted(currentColumn.kind())
                );
            }

            new BlockDAO(connection).block(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void unblock(final Long id, final String reason) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var dto = dao.findById(id).orElseThrow(
                    () -> new EntityNotFoundException(
                            "O card de id %s não foi encontrado".formatted(id))
            );

            if (!dto.blocked()) {
                throw new CardBlockedException(
                        "O card %s não está bloqueado".formatted(id)
                );
            }

            new BlockDAO(connection).unblock(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }
}
