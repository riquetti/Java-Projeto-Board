module org.luis.board {
    requires static lombok;
    requires java.sql;
    requires java.desktop;
    requires liquibase.core;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.yaml.snakeyaml;

    opens org.luis.board to javafx.fxml;
    exports org.luis.board;
}
