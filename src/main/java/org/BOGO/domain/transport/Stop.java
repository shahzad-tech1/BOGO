package org.BOGO.domain.transport;

import java.util.ArrayList;
import java.util.List;


public class Stop {


    private int stopID;
    private String stopName;
    private boolean active;
    private Location location;
    private ArrayList<Stop> connections = new ArrayList<>();
    private ArrayList<Double> connectionsFair = new ArrayList<>();
    private ArrayList<Integer> connectionRoutesId = new ArrayList<>();


    // ---------- Constructors ----------
    public Stop() {}

    //---------------Getters-------------//

    public int  getStopID() { return stopID; }
    public ArrayList<Double> getConnectionsFair() { return connectionsFair; }
    public ArrayList<Integer> getConnectionRoutesId() { return connectionRoutesId; }
    public boolean isActive() { return active; }

    public void initializeStop(int stopID, String stopName, Location location){
        this.stopID = stopID;
        this.stopName = stopName;
        this.active = true;
        this.location = location;
        this.connections = new ArrayList<>();
        this.connectionsFair = new ArrayList<>();
    }

    public void addConnection(Stop stop, double price, int routeId) {
        if(connections.isEmpty()) {
            connections.add(stop);
            connectionsFair.add(price);
            connectionRoutesId.add(routeId);
        }
        else {
            HelpingFunctions h = new HelpingFunctions();
            int index = h.findStopIndex(connections,stop.getStopID());
            if(index < 0) {
                connections.add(stop);
                connectionsFair.add(price);
                connectionRoutesId.add(routeId);
            }
            else {
                System.out.println("Stop Exists Already");
            }

        }

    }

    //------------------Getter---------------
    public String getStopName() { return stopName; }
    public ArrayList<Stop> getConnections() { return connections; }
    public Location getLocation() { return location; }
    public void setStopID(int stopID) { this.stopID = stopID; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    public void setActive(boolean active) { this.active = active; }
    public void setLocation(Location location) { this.location = location; }
    public void setConnections(ArrayList<Stop> connections) { this.connections = connections; }
    public void setConnectionsFair(ArrayList<Double> connectionsFair) { this.connectionsFair = connectionsFair; }
    public void setConnectionRoutesId(ArrayList<Integer> connectionRoutesId) { this.connectionRoutesId = connectionRoutesId; }

    //-----------------Displayer-------------
    public void displayStopDetails() {
        System.out.println("Stop Details of Stop: " + stopID);
        System.out.println("Stop Name: " + stopName);
        System.out.println("Stop is " + (active? "Working" : "Closed"));
        System.out.println("Stop's Location: ");
        location.displayLocation();
        System.out.println("Stop's Connections with fairs are below: ");
        for(int i = 1; i <= connections.size(); i++) {
            System.out.println("Stop's Connection " + i + " Name: " +
                    connections.get(i-1).getStopName() + " Fair : " +
                    connectionsFair.get(i-1) + " And Its follow Route " + connectionRoutesId.get(i-1));
        }

    }

    @Override
    public String toString() {
        return stopName;
    }



}
