package org.luis.board;


import org.luis.board.persistence.migrations.MigrationStrategy;

import java.sql.SQLException;

import static org.luis.board.persistence.config.ConnectionConfig.getConnection;

public class HelloApplication {

    public static void main(String[] args) throws SQLException {
        try(var connection = getConnection()){
            new MigrationStrategy(connection).executeMigration();
        }
    }
}