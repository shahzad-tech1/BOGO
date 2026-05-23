package org.BOGO.domain.transport;

import java.util.ArrayList;

public class Map {

    private ArrayList<Stop> stops = new ArrayList<>();
    private ArrayList<Integer> stopIDs = new ArrayList<>();

    private ArrayList<Route> routes = new ArrayList<>();
    private ArrayList<Integer> routeIDs = new ArrayList<>();

    //----------Constructor------------
    public Map() {}

    //-----------Setter-----------------
    public void addRoute(int routeId) {

        if(routes.isEmpty()) {
            Route route = new Route();
            route.initializeRoute(routeId);
            routes.add(route);
        }
        else {
            HelpingFunctions h = new HelpingFunctions();
            int index = h.findRouteIndex(routes,routeId);
            if(index < 0) {
                Route route = new Route();
                route.initializeRoute(routeId);
                routes.add(route);
            }
            else
                System.out.println("Stop Exists Already");

        }
    }

    //------------Getter--------------------
    public Stop getStop(int i) {return stops.get(i);}
    public ArrayList<Stop> getStops() { return stops; }
    public ArrayList<Route> getRoutes() { return routes; }
    public void setStops(ArrayList<Stop> stops) { this.stops = stops; }
    public void setRoutes(ArrayList<Route> routes) { this.routes = routes; }
    public Stop getStopById(int id) {
        HelpingFunctions h = new HelpingFunctions();
        int index = h.findStopIndex(stops, id);
        if(index < 0){
            return null;
        }
        return stops.get(index);
    }

    public Route getRouteById(int id) {
        HelpingFunctions h = new HelpingFunctions();
        int index = h.findRouteIndex(routes, id);
        if(index < 0){
            return null;
        }
        return routes.get(index);
    }

    //----------Graph Implementation-----------

    public void addStop(int stopID, String stopName, Location location,
                        ArrayList<Integer> connection, ArrayList<Double> price,
                        ArrayList<Integer> routesID) {
        if(stops.isEmpty()) {
            Stop stop = new Stop();
            stop.initializeStop(stopID, stopName, location);
            stops.add(stop);
        }
        else {
            HelpingFunctions h = new HelpingFunctions();
            int index = h.findStopIndex(stops,stopID);
            if(index < 0) {

                Stop stop = new Stop();
                stop.initializeStop(stopID, stopName, location);

                for(int i = 0; i < connection.size();i++) {

                    int connectionsIndex = h.findStopIndex(stops,connection.get(i));     //Find the Stop from Map
                    stop.addConnection(stops.get(connectionsIndex), price.get(i), routesID.get(i));       //Add connection to map's stop
                    stops.get(connectionsIndex).addConnection(stop, price.get(i), routesID.get(i));      //Map's stop is added to connections

                    addRoute(routesID.get(i));
                    int routeIndex = h.findRouteIndex(routes, routesID.get(i));
                    routes.get(routeIndex).addStops(stops.get(connectionsIndex));
                    routes.get(routeIndex).addStops(stop);

                }

                stops.add(stop);
            }
            else
                System.out.println("Stop Exists Already");

        }
    }


    //--------------Displayer----------------
    public void displayMap() {
        System.out.println("Routes Are: ");
        for (int i=0; i < routes.size(); i++) {
            routes.get(i).displayRouteDetails();
        }
        System.out.println("Stops Are: ");
        for (int i=0; i < stops.size();i++) {
            stops.get(i).displayStopDetails();
        }
    }

}
