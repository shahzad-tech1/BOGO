package org.BOGO.domain.transport;

import java.util.ArrayList;
import java.util.List;


public class Route {

    private int routeID;
    private String routeName;
    private boolean active = true;
    private int estimatedTimePerStop = 120; // seconds between stops for simulation
    private ArrayList<Stop> stops = new ArrayList<>();
    private ArrayList<Integer> stopIDs = new ArrayList<>();

    // ---------- Constructors ----------
    public Route() {}
    public Route(int routeID) {
        this.routeID = routeID;
    }


    //----------------Initialize Route-------------
    public void initializeRoute(int routeID) {
        this.routeID = routeID;
    }

    //-----------------Getter--------------------
    public int getRouteID() { return routeID; }
    public String getRouteName() { return routeName; }
    public boolean isActive() { return active; }
    public int getEstimatedTimePerStop() { return estimatedTimePerStop; }
    public ArrayList<Stop> getStops() { return stops; }
    public ArrayList<Integer> getStopIDs() { return stopIDs; }
    public void setRouteID(int routeID) { this.routeID = routeID; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public void setActive(boolean active) { this.active = active; }
    public void setEstimatedTimePerStop(int estimatedTimePerStop) { this.estimatedTimePerStop = estimatedTimePerStop; }
    public void setStops(ArrayList<Stop> stops) { this.stops = stops; }
    public void setStops(java.util.List<Stop> stops) { this.stops = new ArrayList<>(stops); }
    public void setStopIDs(ArrayList<Integer> stopIDs) { this.stopIDs = stopIDs; }

    //---------------Add Stops on Route-------------
    public void addStops(Stop stop) {
        if(stops.isEmpty()) {
            stops.add(stop);
        }
        else {
            HelpingFunctions h = new HelpingFunctions();
            int index = h.findStopIndex(stops,stop.getStopID());
            if(index < 0)
                stops.add(stop);
            else
                System.out.println("Stop Exists Already");
        }

    }


    //-----------------Displayer--------------------
    public void displayRouteDetails() {
        System.out.print("RouteID: " + routeID);
        System.out.print(" Stops: ");
        for (int i = 0; i < stops.size(); i++){
            System.out.print(stops.get(i).getStopID() + " -> ");
        }
        System.out.print("\n");
    }




}
