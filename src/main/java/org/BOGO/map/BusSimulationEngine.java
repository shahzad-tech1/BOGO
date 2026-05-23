package org.BOGO.map;

import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.transport.Trip;
import java.util.List;

/**
 * Pure Java math engine — NO JavaFX, NO Thread, NO ScheduledExecutorService.
 * Called every frame from an AnimationTimer.handle() which already runs on the
 * FX Application Thread, making all drawing calls safe.
 *
 * Placed in org.BOGO.map to distinguish from the old broken engine in
 * org.BOGO.simulation.BusSimulationEngine (which used ScheduledExecutorService
 * and caused UI freezes).
 */
public class BusSimulationEngine {

    private static final double PROGRESS_PER_SECOND = 1.0 / 30.0; // 30s per stop segment
    private static final int    DWELL_TICKS         = 15;          // ~3s pause at each stop @5fps
    private static final double NANOS_PER_SECOND    = 1_000_000_000.0;

    private long lastUpdateNanos = -1;

    private final SimulationState    state;
    private final CoordinateNormalizer normalizer;

    public BusSimulationEngine(SimulationState state, CoordinateNormalizer normalizer) {
        this.state      = state;
        this.normalizer = normalizer;
    }

    /**
     * Called every frame by AnimationTimer.handle(now).
     * @param now current time in nanoseconds (from AnimationTimer)
     * @return true if any bus position changed (caller should redraw canvas)
     */
    public boolean tick(long now) {
        if (lastUpdateNanos < 0) {
            lastUpdateNanos = now;
            return false;
        }

        double deltaSeconds = (now - lastUpdateNanos) / NANOS_PER_SECOND;
        lastUpdateNanos = now;

        // Cap delta to prevent huge jumps after a tab switch / pause
        if (deltaSeconds > 0.5) deltaSeconds = 0.5;

        boolean anyChange = false;

        for (int tripId : state.getTripIds()) {
            Trip  trip  = state.getTripMap().get(tripId);
            Route route = trip.getRoute();
            if (route == null) continue;

            List<Stop> stops = route.getStops();
            if (stops == null || stops.size() < 2) continue;

            // Handle dwell at stop
            int dwell = state.getDwell(tripId);
            if (dwell > 0) {
                state.setDwell(tripId, dwell - 1);
                continue;
            }

            int idx = state.getStopIndex(tripId);

            // Last stop reached — loop back to the first stop
            if (idx >= stops.size() - 1) {
                state.setStopIndex(tripId, 0);
                state.setProgress(tripId, 0.0);
                state.setDwell(tripId, DWELL_TICKS);
                anyChange = true;
                continue;
            }

            double progress = state.getProgress(tripId) + (PROGRESS_PER_SECOND * deltaSeconds);

            if (progress >= 1.0) {
                // Arrived at next stop
                int newIdx = idx + 1;
                state.setStopIndex(tripId, newIdx);
                state.setProgress(tripId, 0.0);
                state.setDwell(tripId, DWELL_TICKS);

                Stop arrived = stops.get(newIdx);
                if (arrived.getLocation() != null)
                    state.setPixelPosition(tripId, new double[]{
                        normalizer.toPixelX(arrived.getLocation().getLongitude()),
                        normalizer.toPixelY(arrived.getLocation().getLatitude())
                    });
            } else {
                // Interpolate between current and next stop
                state.setProgress(tripId, progress);
                Stop from = stops.get(idx);
                Stop to   = stops.get(idx + 1);
                if (from.getLocation() != null && to.getLocation() != null)
                    state.setPixelPosition(tripId, normalizer.interpolatePixel(from, to, progress));
            }
            anyChange = true;
        }
        return anyChange;
    }

    /** Call in onShow() to reset delta timer and prevent position jumps. */
    public void reset() { lastUpdateNanos = -1; }
}
