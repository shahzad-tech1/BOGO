package org.BOGO.controller;

import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import org.BOGO.map.*;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;
import org.BOGO.repository.TripRepository;

/**
 * Controller for the General Map shown on the Admin / Driver / Passenger dashboards.
 * Uses AnimationTimer (already on FX thread) — never freezes the UI.
 *
 * Usage from a parent FXML controller:
 *   GeneralMapController gmc = new GeneralMapController(canvas, statusLabel);
 *   gmc.initialize();
 *   // on tab hide: gmc.onHide();   on tab show: gmc.onShow();
 */
public class GeneralMapController {

    private final Canvas canvas;
    private final Label  statusLabel; // may be null

    private final StopRepository  stopRepo  = new StopRepository();
    private final RouteRepository routeRepo = new RouteRepository();
    private final TripRepository  tripRepo  = new TripRepository();

    private CoordinateNormalizer normalizer;
    private SimulationState      simState;
    private BusSimulationEngine  engine;
    private GeneralMapRenderer   renderer;
    private MapDataLoader        dataLoader;
    private AnimationTimer       animationTimer;

    public GeneralMapController(Canvas canvas, Label statusLabel) {
        this.canvas      = canvas;
        this.statusLabel = statusLabel;
    }

    /** Call once after the Canvas has been laid out (so width/height > 0). */
    public void initialize() {
        setStatus("Loading map…");

        // Step 1: load all data on a BACKGROUND thread
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                dataLoader = new MapDataLoader(stopRepo, routeRepo, tripRepo);
                dataLoader.loadAll();
                return null;
            }
        };

        // Step 2: after data loaded, set up renderer + AnimationTimer on FX thread
        loadTask.setOnSucceeded(e -> {
            if (!dataLoader.isLoaded() || dataLoader.getAllStops().isEmpty()) {
                setStatus("No map data available.");
                return;
            }
            setupSimulation();
            setStatus("");
        });

        loadTask.setOnFailed(e -> setStatus("Failed to load map."));

        Thread t = new Thread(loadTask, "GeneralMapLoader");
        t.setDaemon(true);
        t.start();

        // Step 3: stop animation when pane is removed from scene
        canvas.sceneProperty().addListener((obs, old, newScene) -> {
            if (newScene == null && animationTimer != null) animationTimer.stop();
            else if (newScene != null && animationTimer != null) animationTimer.start();
        });
    }

    private void setupSimulation() {
        normalizer = new CoordinateNormalizer();
        normalizer.calibrate(dataLoader.getAllStops(), canvas.getWidth(), canvas.getHeight());

        // Re-calibrate on resize
        canvas.widthProperty().addListener((obs, o, n) ->
            normalizer.calibrate(dataLoader.getAllStops(), n.doubleValue(), canvas.getHeight()));
        canvas.heightProperty().addListener((obs, o, n) ->
            normalizer.calibrate(dataLoader.getAllStops(), canvas.getWidth(), n.doubleValue()));

        simState = new SimulationState();
        simState.initializeTrips(dataLoader.getActiveTrips());

        engine   = new BusSimulationEngine(simState, normalizer);
        renderer = new GeneralMapRenderer(canvas, normalizer);

        // AnimationTimer runs on FX thread — safe for Canvas drawing, no freeze
        animationTimer = new AnimationTimer() {
            private long lastFrame = 0;
            private static final long FRAME_INTERVAL = 100_000_000L; // 10 fps

            @Override
            public void handle(long now) {
                if (now - lastFrame < FRAME_INTERVAL) return;
                lastFrame = now;
                engine.tick(now);
                renderer.draw(dataLoader.getAllStops(), dataLoader.getAllRoutes(), simState);
            }
        };
        animationTimer.start();
    }

    /** Call when the user navigates away from this map tab. */
    public void onHide() { if (animationTimer != null) animationTimer.stop(); }

    /** Call when the user navigates back to this map tab. */
    public void onShow() {
        if (animationTimer != null) animationTimer.start();
        if (engine != null) engine.reset(); // prevent position jump after pause
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}
