module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires kotlin.stdlib;

    opens org.example.infrastructure.ui to javafx.fxml;
    exports org.example;
}