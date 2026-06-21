module com.energycommunity.javafxgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    opens com.energycommunity.javafxgui to javafx.fxml;
    exports com.energycommunity.javafxgui;
}
