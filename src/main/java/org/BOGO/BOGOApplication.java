package org.BOGO;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.BOGO.controller.BookingController;
import org.BOGO.controller.MessageController;
import org.BOGO.controller.ResourceController;
import org.BOGO.controller.UserController;
import org.BOGO.domain.transport.Map;
import org.BOGO.domain.user.User;
import org.BOGO.service.BusAllocationService;
import org.BOGO.service.PathBuildingService;
import org.BOGO.simulation.BusSimulationEngine;

import java.io.IOException;
import java.util.Objects;

public class BOGOApplication extends Application {
    private static BOGOApplication instance;

    private final UserController userController = new UserController();
    private final BookingController bookingController = new BookingController();
    private final ResourceController resourceController = new ResourceController();
    private final MessageController messageController = new MessageController();
    private final PathBuildingService pathBuildingService = new PathBuildingService();
    private User currentUser;
    private Map transportMap;
    private BusSimulationEngine simulationEngine;

    public BOGOApplication() {
        instance = this;
    }

    public static BOGOApplication getInstance() {
        if (instance == null) {
            instance = new BOGOApplication();
        }
        return instance;
    }

    @Override
    public void start(Stage stage) throws IOException {
        transportMap = pathBuildingService.initializeFromDatabase();
        FXMLLoader loader = new FXMLLoader(BOGOApplication.class.getResource("/Main.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 760);
        scene.getStylesheets().add(Objects.requireNonNull(
                BOGOApplication.class.getResource("/NeonTheme.css")
        ).toExternalForm());
        stage.setTitle("BOGO Bus Management System");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();

        // Auto-assign spare buses to spare drivers (daemon thread — never blocks UI)
        Thread autoAssign = new Thread(
                () -> new BusAllocationService().autoAssign(), "BusAutoAssign");
        autoAssign.setDaemon(true);
        autoAssign.start();
    }

    public UserController getUserController() {
        return userController;
    }

    public BookingController getBookingController() {
        return bookingController;
    }

    public ResourceController getResourceController() {
        return resourceController;
    }

    public MessageController getMessageController() {
        return messageController;
    }

    public PathBuildingService getPathBuildingService() {
        return pathBuildingService;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Map getTransportMap() {
        return transportMap;
    }

    public void setTransportMap(Map transportMap) {
        this.transportMap = transportMap;
    }

    public BusSimulationEngine getSimulationEngine() {
        return simulationEngine;
    }

    public void setSimulationEngine(BusSimulationEngine simulationEngine) {
        this.simulationEngine = simulationEngine;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
