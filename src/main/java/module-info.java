module com.warehouse {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    exports com.warehouse to javafx.graphics;
    opens com.warehouse to javafx.fxml;
    opens com.warehouse.controller to javafx.fxml;
    opens com.warehouse.model to javafx.base;
}
