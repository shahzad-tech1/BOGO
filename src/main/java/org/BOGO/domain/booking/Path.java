package org.BOGO.domain.booking;

import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a computed passenger journey path.
 * A path can span multiple routes (multi-leg travel).
 */
public class Path {

    private int pathId;
    private Stop origin;
    private Stop destination;
    private List<Stop> stops = new ArrayList<>();
    private List<Route> routes = new ArrayList<>();
    private double totalCost;
    private int totalEstimatedTime;
    private List<String> transferInstructions = new ArrayList<>();

    // ---------- Constructors ----------
    public Path() {}

    public Path(int pathId, Stop origin, Stop destination) {
        this.pathId = pathId;
        this.origin = origin;
        this.destination = destination;
    }

    // ---------- Getters ----------
    public int getPathId() { return pathId; }
    public Stop getOrigin() { return origin; }
    public Stop getDestination() { return destination; }
    public List<Stop> getStops() { return stops; }
    public List<Route> getRoutes() { return routes; }
    public double getTotalCost() { return totalCost; }
    public int getTotalEstimatedTime() { return totalEstimatedTime; }
    public List<String> getTransferInstructions() { return transferInstructions; }

    public boolean isMultiLeg() {
        return routes != null && routes.size() > 1;
    }

    // ---------- Setters ----------
    public void setPathId(int pathId) { this.pathId = pathId; }
    public void setOrigin(Stop origin) { this.origin = origin; }
    public void setDestination(Stop destination) { this.destination = destination; }
    public void setStops(List<Stop> stops) { this.stops = stops; }
    public void setRoutes(List<Route> routes) { this.routes = routes; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    public void setTotalEstimatedTime(int totalEstimatedTime) { this.totalEstimatedTime = totalEstimatedTime; }
    public void setTransferInstructions(List<String> transferInstructions) { this.transferInstructions = transferInstructions; }

    public void addStop(Stop stop) {
        if (stops == null) stops = new ArrayList<>();
        stops.add(stop);
    }

    public void addRoute(Route route) {
        if (routes == null) routes = new ArrayList<>();
        routes.add(route);
    }

    public void addTransferInstruction(String instruction) {
        if (transferInstructions == null) transferInstructions = new ArrayList<>();
        transferInstructions.add(instruction);
    }

    @Override
    public String toString() {
        return "Path[" + pathId + "] " + (origin != null ? origin.getStopName() : "?")
                + " -> " + (destination != null ? destination.getStopName() : "?")
                + " (" + (stops != null ? stops.size() : 0) + " stops, cost=" + totalCost + ")";
    }
}
