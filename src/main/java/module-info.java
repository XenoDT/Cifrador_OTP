module com.dgomez.testing {
    requires javafx.controls;
    requires org.junit.jupiter.api;

    exports com.dgomez.testing;
    exports com.dgomez.testing.service;
    opens com.dgomez.testing.service to org.junit.platform.commons;
}