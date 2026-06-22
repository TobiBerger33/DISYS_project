module com.energycommunity.javafxgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.net.http;

    opens com.energycommunity.javafxgui to javafx.fxml;
    exports com.energycommunity.javafxgui;
}
