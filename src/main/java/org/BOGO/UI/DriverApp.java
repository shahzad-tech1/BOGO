package org.BOGO.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.BOGO.BOGOApplication;
import org.BOGO.simulation.BusSimulationEngine;

import java.util.Objects;

/**
 * DriverApp — Entry point for the bogo-driver.jar.
 * Opens directly to the Driver sign-in screen.
 */
public class DriverApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        BOGOApplication.getInstance();

        // Start simulation engine so Driver dashboard map receives live ticks
        BusSimulationEngine engine = new BusSimulationEngine();
        engine.start();
        BOGOApplication.getInstance().setSimulationEngine(engine);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DriverUI/DriverSignIn.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 760);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/NeonTheme.css")).toExternalForm());
        stage.setTitle("BOGO — Driver Console");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> engine.stop());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
