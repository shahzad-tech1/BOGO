package org.BOGO.map;

import org.BOGO.domain.transport.Trip;
import java.util.*;

/**
 * Pure Java (no JavaFX) — holds the current interpolated position of every bus.
 * Updated by BusSimulationEngine; read by Renderer. Thread-safe enough for
 * single-threaded AnimationTimer usage.
 */
public class SimulationState {

    private final Map<Integer, Double>   progressMap     = new HashMap<>();
    private final Map<Integer, Integer>  stopIndexMap    = new HashMap<>();
    private final Map<Integer, Integer>  dwellMap        = new HashMap<>();
    private final Map<Integer, double[]> pixelPositionMap = new HashMap<>();
    private final Map<Integer, Trip>     tripMap         = new HashMap<>();

    public void initializeTrips(List<Trip> trips) {
        progressMap.clear(); stopIndexMap.clear(); dwellMap.clear();
        pixelPositionMap.clear(); tripMap.clear();
        for (Trip trip : trips) {
            int id = trip.getTripID();   // NOTE: getTripID() not getTripId()
            tripMap.put(id, trip);
            progressMap.put(id, 0.0);
            stopIndexMap.put(id, 0);
            dwellMap.put(id, 0);
            pixelPositionMap.put(id, new double[]{0, 0});
        }
    }

    public Map<Integer, Trip> getTripMap()                   { return tripMap; }
    public double   getProgress(int tripId)                  { return progressMap.getOrDefault(tripId, 0.0); }
    public void     setProgress(int tripId, double p)        { progressMap.put(tripId, p); }
    public int      getStopIndex(int tripId)                 { return stopIndexMap.getOrDefault(tripId, 0); }
    public void     setStopIndex(int tripId, int idx)        { stopIndexMap.put(tripId, idx); }
    public int      getDwell(int tripId)                     { return dwellMap.getOrDefault(tripId, 0); }
    public void     setDwell(int tripId, int d)              { dwellMap.put(tripId, d); }
    public double[] getPixelPosition(int tripId)             { return pixelPositionMap.getOrDefault(tripId, new double[]{0,0}); }
    public void     setPixelPosition(int tripId, double[] p) { pixelPositionMap.put(tripId, p); }
    public Set<Integer> getTripIds()                         { return tripMap.keySet(); }
}
