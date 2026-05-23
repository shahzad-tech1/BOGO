package org.BOGO.domain.transport;

import java.util.ArrayList;

public class HelpingFunctions {

    public int findStopIndex(ArrayList<Stop> stops, int stopId ) {

        for(int i = 0; i < stops.size(); i++) {
            if (stopId == stops.get(i).getStopID()){
                return i;
            }
        }
        return -1;
    }

    public int findRouteIndex(ArrayList<Route> routes, int routeId ) {

        for(int i = 0; i < routes.size(); i++) {
            if (routeId == routes.get(i).getRouteID()){
                return i;
            }
        }
        return -1;
    }
}
