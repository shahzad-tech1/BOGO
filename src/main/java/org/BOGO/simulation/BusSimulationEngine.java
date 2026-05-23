package org.BOGO.simulation;

import javafx.application.Platform;
import org.BOGO.domain.transport.*;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.TripRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * BusSimulationEngine runs on a background daemon thread.
 *
 * On each tick (every TICK_INTERVAL_MS milliseconds):
 *  1. Fetches all IN_PROGRESS trips from the DB.
 *  2. For each trip, interpolates the bus position between the current and next stop.
 *  3. Advances the stop index when progress reaches 1.0.
 *  4. Marks the trip COMPLETED when the last stop is reached.
 *  5. Calls any registered UIUpdaters via Platform.runLater() for safe JavaFX updates.
 *
 * Health metrics degrade slowly over time to reflect wear on the vehicle.
 */
public class BusSimulationEngine {

    private static final long TICK_INTERVAL_MS = 2000; // 2 seconds per tick
    private static final double PROGRESS_PER_TICK = 0.1; // 10% of inter-stop distance per tick
    private static final double HEALTH_DEGRADATION_PER_TICK = 0.02; // 2% degradation per 100 ticks

    private final TripRepository tripRepository = new TripRepository();
    private final BusRepository busRepository = new BusRepository();
    private final org.BOGO.repository.RouteRepository routeRepository = new org.BOGO.repository.RouteRepository();
    private final org.BOGO.repository.StopRepository stopRepository = new org.BOGO.repository.StopRepository();

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> taskHandle;
    private volatile boolean running = false;

    // Listeners notified on each tick (called on JavaFX thread via Platform.runLater)
    private final CopyOnWriteArrayList<SimulationListener> listeners = new CopyOnWriteArrayList<>();

    private long tickCount = 0;

    public interface SimulationListener {
        void onSimulationTick(List<Trip> activeTrips);
    }

    // ---------- Lifecycle ----------

    public void start() {
        if (running) {
            System.out.println("[BusSimulationEngine] Already running.");
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "BusSimEngine");
            t.setDaemon(true);
            return t;
        });
        taskHandle = scheduler.scheduleAtFixedRate(this::tick, 0, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
        running = true;
        System.out.println("[BusSimulationEngine] Started (tick=" + TICK_INTERVAL_MS + "ms).");
    }

    public void stop() {
        if (!running) return;
        if (taskHandle != null) taskHandle.cancel(false);
        if (scheduler != null) scheduler.shutdownNow();
        running = false;
        System.out.println("[BusSimulationEngine] Stopped.");
    }

    public boolean isRunning() { return running; }

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    // ---------- Core Tick ----------

    private void tick() {
        try {
            tickCount++;
            List<Trip> activeTrips = tripRepository.findLiveTripsWithBuses();

            for (Trip trip : activeTrips) {
                advanceTripSimulation(trip);
            }

            // Notify UI listeners on the JavaFX thread
            if (!listeners.isEmpty()) {
                final List<Trip> snapshot = activeTrips;
                Platform.runLater(() -> {
                    for (SimulationListener l : listeners) {
                        l.onSimulationTick(snapshot);
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("[BusSimulationEngine] Tick error: " + e.getMessage());
        }
    }

    private void advanceTripSimulation(Trip trip) {
        Route route = routeRepository.findById(trip.getRouteID());
        if (route == null || route.getStopIDs().isEmpty()) return;

        List<Integer> stopIds = route.getStopIDs();
        int stopCount = stopIds.size();
        int currentIndex = trip.getCurrentStopIndex();
        double progress = trip.getSimulatedProgress();

        // If at or past the last stop, mark complete and auto-restart
        if (currentIndex >= stopCount - 1) {
            tripRepository.updateStatus(trip.getTripID(), "COMPLETED");
            System.out.println("[BusSimulationEngine] Trip " + trip.getTripID() + " COMPLETED.");
            autoRestartTrip(trip);   // immediately restart the same route
            return;
        }

        // Advance progress
        progress += PROGRESS_PER_TICK;

        if (progress >= 1.0) {
            // Arrived at next stop — advance stop index
            currentIndex++;
            progress = 0.0;
            System.out.println("[BusSimulationEngine] Trip " + trip.getTripID()
                    + " arrived at stop index=" + currentIndex);
        }

        // Persist progress
        tripRepository.updateProgress(trip.getTripID(), currentIndex, progress);
        trip.setCurrentStopIndex(currentIndex);
        trip.setSimulatedProgress(progress);

        // Interpolate and update bus GPS position
        updateBusPosition(trip, route, currentIndex, progress);

        // Degrade health metrics periodically
        if (tickCount % 50 == 0 && trip.getBus() != null) {
            degradeBusHealth(trip.getBus());
        }
    }

    /**
     * Interpolates the bus lat/lon between the current and next stop and persists it.
     */
    private void updateBusPosition(Trip trip, Route route, int stopIndex, double progress) {
        if (trip.getBus() == null) return;

        List<Integer> stopIds = route.getStopIDs();
        if (stopIndex >= stopIds.size() - 1) return;

        Stop fromStop = stopRepository.findById(stopIds.get(stopIndex));
        Stop toStop   = stopRepository.findById(stopIds.get(stopIndex + 1));

        if (fromStop == null || toStop == null) return;

        Location from = fromStop.getLocation();
        Location to   = toStop.getLocation();
        if (from == null || to == null) return;

        double lat = from.getLatitude()  + (to.getLatitude()  - from.getLatitude())  * progress;
        double lon = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * progress;

        busRepository.updateLocation(trip.getBusID(), lat, lon);
        trip.getBus().setLocation(new Location(lat, lon));
    }

    private void degradeBusHealth(Bus bus) {
        double newTyre    = Math.max(0, bus.getTyreHealth()    - HEALTH_DEGRADATION_PER_TICK);
        double newEngine  = Math.max(0, bus.getEngineHealth()  - HEALTH_DEGRADATION_PER_TICK);
        double newChassis = Math.max(0, bus.getChassisHealth() - HEALTH_DEGRADATION_PER_TICK);

        busRepository.updateHealthMetrics(bus.getBusID(), newTyre, newEngine, newChassis);
        bus.setTyreHealth(newTyre);
        bus.setEngineHealth(newEngine);
        bus.setChassisHealth(newChassis);
    }

    /**
     * After a trip is COMPLETED, reset the bus GPS to the route's first stop
     * and create a new IN_PROGRESS trip for the same route/bus/driver.
     * The new trip is picked up automatically on the next tick.
     */
    private void autoRestartTrip(Trip completedTrip) {
        try {
            // Reset bus position to the first stop of the route
            Route route = routeRepository.findById(completedTrip.getRouteID());
            if (route != null && !route.getStopIDs().isEmpty()) {
                Stop firstStop = stopRepository.findById(route.getStopIDs().get(0));
                if (firstStop != null && firstStop.getLocation() != null) {
                    busRepository.updateLocation(
                            completedTrip.getBusID(),
                            firstStop.getLocation().getLatitude(),
                            firstStop.getLocation().getLongitude()
                    );
                }
            }
            // Insert a new trip row (same route / bus / driver)
            int newId = tripRepository.save(
                    completedTrip.getRouteID(),
                    completedTrip.getBusID(),
                    completedTrip.getDriverID()
            );
            if (newId > 0) {
                System.out.println("[BusSimulationEngine] Auto-restarted trip " + completedTrip.getTripID()
                        + " → new TripId=" + newId);
            } else {
                System.err.println("[BusSimulationEngine] Auto-restart failed for TripId=" + completedTrip.getTripID());
            }
        } catch (Exception e) {
            System.err.println("[BusSimulationEngine] autoRestartTrip error: " + e.getMessage());
        }
    }
}
