package org.BOGO.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.BOGO.BOGOApplication;
import org.BOGO.simulation.BusSimulationEngine;

import java.util.Objects;

/**
 * PassengerApp — Entry point for the bogo-passenger.jar.
 * Opens directly to the Passenger login / sign-up screen.
 */
public class PassengerApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        BOGOApplication.getInstance();

        // Start simulation engine so Passenger dashboard map receives live ticks
        BusSimulationEngine engine = new BusSimulationEngine();
        engine.start();
        BOGOApplication.getInstance().setSimulationEngine(engine);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/PassengerUI/PassengerLogin_SignUp.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 760);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/NeonTheme.css")).toExternalForm());
        stage.setTitle("BOGO – Passenger Portal");
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
