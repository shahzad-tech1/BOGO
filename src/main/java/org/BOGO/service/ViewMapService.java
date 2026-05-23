package org.BOGO.service;

import org.BOGO.domain.transport.Location;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;

import java.util.List;

public class ViewMapService {

    private final RouteRepository routeRepository = new RouteRepository();
    private final StopRepository stopRepository = new StopRepository();


    /**
     * Fetches and returns all stops and active routes for map rendering.
     */
    public Object loadMap() {
        org.BOGO.domain.transport.Map map = new org.BOGO.domain.transport.Map();
        map.setStops(new java.util.ArrayList<>(stopRepository.findAll()));
        map.setRoutes(new java.util.ArrayList<>(routeRepository.findAll()));
        return map;
    }

    /**
     * Returns the current GPS locations of all active buses.
     */
    public List<Location> getBusLocations() {
        return java.util.List.of();
    }

    /**
     * Returns all stops to be rendered on the map.
     */
    public List<Stop> getStopsOnMap() {
        return stopRepository.findAll();
    }

    /**
     * Pushes updated bus position data to connected map clients.
     */
    public void refreshBusPositions() {}

    /**
     * Returns the full details of a specific stop by ID.
     */
    public Stop getStopDetails(int stopID) {
        return stopRepository.findById(stopID);
    }

    /**
     * Returns all currently active routes for map overlay.
     */
    public List<Route> getActiveRoutes() {
        return routeRepository.findAll();
    }
}
