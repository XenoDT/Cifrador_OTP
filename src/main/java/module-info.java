module com.dgomez.testing {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.dgomez.testing to javafx.fxml;
    exports com.dgomez.testing;
    exports com.dgomez.testing.service;
    opens com.dgomez.testing.service to javafx.fxml;
}