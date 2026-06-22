package com.energycommunity.javafxgui;

import javafx.application.Application;

/**
 * Plain launcher for {@link HelloApplication}. A separate main class that does
 * not extend Application is the standard workaround for starting a JavaFX app
 * without module-path/classpath errors.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}
