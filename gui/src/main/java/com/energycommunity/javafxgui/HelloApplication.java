package com.energycommunity.javafxgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application for the label-based GUI (hello-view.fxml + HelloController).
 * This is the entry point configured in the pom's javafx-maven-plugin, i.e. the
 * GUI started by {@code mvn javafx:run}.
 */
public class HelloApplication extends Application {

    // Called by JavaFX once the toolkit is ready; loads the FXML and shows the window.
    @Override
    public void start(Stage stage) throws Exception {
        // Resource is resolved relative to this class's package (com/energycommunity/javafxgui).
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("hello-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 420, 480);
        stage.setTitle("Energy Community Monitor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}