package com.disys.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application for the table-based GUI (main.fxml + MainController).
 * Note: this is the alternate GUI variant. The entry point configured in the
 * pom's javafx plugin is {@code com.energycommunity.javafxgui.HelloApplication}.
 */
public class GuiApplication extends Application {

    // Called by JavaFX once the UI toolkit is ready; builds and shows the window.
    @Override
    public void start(Stage stage) throws Exception {
        // Load the layout from FXML; the fx:controller in the FXML wires up MainController.
        FXMLLoader loader = new FXMLLoader(GuiApplication.class.getResource("/com/disys/gui/main.fxml"));
        Scene scene = new Scene(loader.load(), 800, 500);
        stage.setTitle("Energy Community Monitor");
        stage.setScene(scene);
        stage.show();
    }
}