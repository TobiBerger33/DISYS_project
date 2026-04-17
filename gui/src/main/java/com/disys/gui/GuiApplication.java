package com.disys.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(GuiApplication.class.getResource("/com/disys/gui/main.fxml"));
        Scene scene = new Scene(loader.load(), 800, 500);
        stage.setTitle("Energy Community Monitor");
        stage.setScene(scene);
        stage.show();
    }

    public static class Launcher {
        public static void main(String[] args) {
            Application.launch(GuiApplication.class, args);
        }
    }
}
