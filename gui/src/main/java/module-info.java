// Java Platform Module System descriptor for the GUI module.
module com.energycommunity.javafxgui {
    // JavaFX UI controls and FXML loading.
    requires javafx.controls;
    requires javafx.fxml;
    // Jackson for parsing the rest-api JSON, incl. java.time support.
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    // Java's built-in HTTP client used by ApiClient/MainController.
    requires java.net.http;

    // Let JavaFX reflectively access the controllers/fields in this package to inject @FXML fields.
    opens com.energycommunity.javafxgui to javafx.fxml;
    exports com.energycommunity.javafxgui;
}
