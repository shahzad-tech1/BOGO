package org.BOGO.domain.transport;

import java.time.LocalDateTime;

public class Trip {
    private int tripID;
    private int busID;
    private int routeID;
    private int driverID;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Bus bus;
    private Route route;

    // Simulation fields (mapped to new DB columns)
    private int currentStopIndex = 0;
    private double simulatedProgress = 0.0; // 0.0 to 1.0 between two stops
    private String tripStatus = "IN_PROGRESS"; // "IN_PROGRESS", "COMPLETED", "CANCELLED"

    public Trip() {
    }

    public Trip(int tripID, int routeID, int busID, int driverID, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.tripID = tripID;
        this.routeID = routeID;
        this.busID = busID;
        this.driverID = driverID;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public int getTripID() { return tripID; }
    public int getBusID() { return busID; }
    public int getRouteID() { return routeID; }
    public int getDriverID() { return driverID; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public Bus getBus() { return bus; }
    public Route getRoute() { return route; }
    public int getCurrentStopIndex() { return currentStopIndex; }
    public double getSimulatedProgress() { return simulatedProgress; }
    public String getTripStatus() { return tripStatus; }

    public void setTripID(int tripID) { this.tripID = tripID; }
    public void setBusID(int busID) { this.busID = busID; }
    public void setRouteID(int routeID) { this.routeID = routeID; }
    public void setDriverID(int driverID) { this.driverID = driverID; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setBus(Bus bus) { this.bus = bus; }
    public void setRoute(Route route) { this.route = route; }
    public void setCurrentStopIndex(int currentStopIndex) { this.currentStopIndex = currentStopIndex; }
    public void setSimulatedProgress(double simulatedProgress) { this.simulatedProgress = simulatedProgress; }
    public void setTripStatus(String tripStatus) { this.tripStatus = tripStatus; }
}
