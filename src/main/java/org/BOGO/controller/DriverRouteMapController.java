package org.BOGO.controller;

import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.transport.Trip;
import org.BOGO.map.*;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;
import org.BOGO.repository.TripRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Driver Route Map — shows ONE bus moving on the driver's
 * assigned route. Wired from BOGOUIController when the Driver_ViewRoute page loads.
 *
 * Usage (from BOGOUIController.loadDriverRoute or initialize):
 *   int driverId = ((Driver) currentUser).getUserID();
 *   DriverRouteMapController ctrl = new DriverRouteMapController(canvas, driverId, statusLabel);
 *   ctrl.initialize();
 *   // On page hide: ctrl.onHide();
 *   // On page show: ctrl.onShow();
 */
public class DriverRouteMapController {

    private final Canvas canvas;
    private final int    driverId;
    private final Label  statusLabel; // may be null

    private final TripRepository  tripRepo  = new TripRepository();
    private final RouteRepository routeRepo = new RouteRepository();
    private final StopRepository  stopRepo  = new StopRepository();

    private CoordinateNormalizer   normalizer;
    private SimulationState        simState;
    private BusSimulationEngine    engine;
    private DriverRouteMapRenderer renderer;
    private AnimationTimer         animationTimer;

    private Route driverRoute;

    public DriverRouteMapController(Canvas canvas, int driverId, Label statusLabel) {
        this.canvas      = canvas;
        this.driverId    = driverId;
        this.statusLabel = statusLabel;
    }

    /** Call once after the canvas has been laid out (so width/height > 0). */
    public void initialize() {
        setStatus("Loading route…");

        // Load driver's active trip on a background thread
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                Trip driverTrip = tripRepo.findActiveByDriverId(driverId);
                if (driverTrip != null) {
                    // Resolve route with full stop objects
                    Route route = routeRepo.findById(driverTrip.getRouteID());
                    if (route != null) {
                        // Resolve stop objects for coordinate normalizer
                        List<Stop> allStops = stopRepo.findAll();
                        java.util.Map<Integer, Stop> stopById = new java.util.HashMap<>();
                        for (Stop s : allStops) stopById.put(s.getStopID(), s);

                        List<Stop> routeStops = new ArrayList<>();
                        for (int id : route.getStopIDs()) {
                            Stop s = stopById.get(id);
                            if (s != null) routeStops.add(s);
                        }
                        route.setStops(new ArrayList<>(routeStops));
                        driverTrip.setRoute(route);
                    }
                    driverRoute = driverTrip.getRoute();
                }
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            if (driverRoute == null || driverRoute.getStops() == null
                    || driverRoute.getStops().size() < 2) {
                setStatus("No active route assigned.");
                return;
            }
            setupSimulation();
            setStatus("");
        });

        loadTask.setOnFailed(e -> setStatus("Error loading route."));

        // Stop animation when pane is removed from scene
        canvas.sceneProperty().addListener((obs, old, newScene) -> {
            if (newScene == null && animationTimer != null) animationTimer.stop();
            else if (newScene != null && animationTimer != null) animationTimer.start();
        });

        Thread t = new Thread(loadTask, "DriverRouteLoader");
        t.setDaemon(true);
        t.start();
    }

    private void setupSimulation() {
        List<Stop> routeStops = driverRoute.getStops();

        normalizer = new CoordinateNormalizer();
        normalizer.calibrate(routeStops, canvas.getWidth(), canvas.getHeight());

        // Re-calibrate on resize
        canvas.widthProperty().addListener((obs, o, n) ->
            normalizer.calibrate(routeStops, n.doubleValue(), canvas.getHeight()));
        canvas.heightProperty().addListener((obs, o, n) ->
            normalizer.calibrate(routeStops, canvas.getWidth(), n.doubleValue()));

        // Single-trip simulation state for this driver's bus
        simState = new SimulationState();
        Trip driverTrip = tripRepo.findActiveByDriverId(driverId);
        if (driverTrip != null) {
            driverTrip.setRoute(driverRoute);
            simState.initializeTrips(List.of(driverTrip));
        }

        engine   = new BusSimulationEngine(simState, normalizer);
        renderer = new DriverRouteMapRenderer(canvas, normalizer);

        // AnimationTimer — runs on FX thread, no freezes possible
        animationTimer = new AnimationTimer() {
            private long lastFrame = 0;
            private static final long FRAME_INTERVAL = 100_000_000L; // 10 fps

            @Override
            public void handle(long now) {
                if (now - lastFrame < FRAME_INTERVAL) return;
                lastFrame = now;
                engine.tick(now);

                // Update bus position from simState
                if (!simState.getTripIds().isEmpty()) {
                    int tripId = simState.getTripIds().iterator().next();
                    double[] pos = simState.getPixelPosition(tripId);
                    renderer.updateBusPosition(pos[0], pos[1]);
                }
                renderer.draw(driverRoute);
            }
        };
        animationTimer.start();
    }

    /** Call when driver navigates away from the View Route page. */
    public void onHide() { if (animationTimer != null) animationTimer.stop(); }

    /** Call when driver navigates back to the View Route page. */
    public void onShow() {
        if (animationTimer != null) animationTimer.start();
        if (engine != null) engine.reset(); // prevent bus position jump
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}
