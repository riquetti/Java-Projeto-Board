package org.luis.board;

import org.luis.board.persistence.migrations.MigrationStrategy;
import org.luis.board.ui.MainMenu;

import java.sql.SQLException;

import static org.luis.board.persistence.config.ConnectionConfig.getConnection;

public class Main {
    public static void main(String[] args) throws SQLException {
        try(var connection = getConnection()){
            new MigrationStrategy(connection).executeMigration();
        }
        new MainMenu().execute();
    }
}
