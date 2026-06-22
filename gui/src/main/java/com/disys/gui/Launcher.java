package com.disys.gui;

import javafx.application.Application;

/**
 * Plain launcher for {@link GuiApplication}. A separate main class that does not
 * itself extend Application is the standard workaround for starting a JavaFX app
 * from a non-modular/fat-jar setup without classpath errors.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(GuiApplication.class, args);
    }
}
