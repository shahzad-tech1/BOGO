package org.BOGO.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.BOGO.BOGOApplication;
import org.BOGO.simulation.BusSimulationEngine;

import java.util.Objects;

/**
 * AdminApp — Entry point for the bogo-admin.jar.
 * Opens directly to AdminLogin screen and starts the simulation engine.
 */
public class AdminApp extends Application {

    private static BusSimulationEngine engine;

    @Override
    public void start(Stage stage) throws Exception {
        // Bootstrap the shared BOGOApplication singleton
        BOGOApplication.getInstance();
        // Load admin login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminUI/AdminLogin.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 760);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/NeonTheme.css")).toExternalForm());
        stage.setTitle("BOGO — Admin Console");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();

        // Start simulation engine for admin's live map
        engine = new BusSimulationEngine();
        engine.start();
        BOGOApplication.getInstance().setSimulationEngine(engine);

        // Stop engine cleanly on close
        stage.setOnCloseRequest(e -> engine.stop());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
