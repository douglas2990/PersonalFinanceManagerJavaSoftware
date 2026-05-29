module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires kotlin.stdlib;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.net.http;

    // Adicione estas linhas:
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Permite que o Jackson acesse suas entidades para converter JSON <-> Objeto
    opens org.example.domain.entity to com.fasterxml.jackson.databind;

    opens org.example.infrastructure.ui to javafx.fxml;
    exports org.example;
}