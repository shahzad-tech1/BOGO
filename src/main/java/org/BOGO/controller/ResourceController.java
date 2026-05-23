package org.BOGO.controller;

import org.BOGO.service.*;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;

import java.util.List;

public class ResourceController {

    private final HandleDriverService  handleDriverService = new HandleDriverService();
    private final RouteReviseService   routeReviseService = new RouteReviseService();
    private final ManageStopsService   manageStopsService = new ManageStopsService();
    private final BusServices         busServices = new BusServices();
    private final TripService         tripService = new TripService();

    public List<Stop> getStopsByRoute(int routeId) {
        return manageStopsService.getStopsByRoute(routeId);
    }

    public List<Stop> getStopsOnMap() {
        return new ViewMapService().getStopsOnMap();
    }

    public List<Route> getActiveRoutes() {
        return new ViewMapService().getActiveRoutes();
    }
}
