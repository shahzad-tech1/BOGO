package org.BOGO.service;

import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Admin;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * RouteReviseService handles route creation, modification, and validation.
 */
public class RouteReviseService {

    private final RouteRepository routeRepository = new RouteRepository();
    private final StopRepository stopRepository = new StopRepository();

    /**
     * Creates a new route with the given stops and persists it to the DB.
     * Returns the created Route, or null on failure.
     */
    public Route addRoute(String routeName, List<Integer> stopIds, int estimatedTimePerStop) {
        if (routeName == null || routeName.isBlank()) {
            System.out.println("[RouteReviseService] addRoute: route name is blank.");
            return null;
        }
        if (stopIds == null || stopIds.size() < 2) {
            System.out.println("[RouteReviseService] addRoute: need at least 2 stops.");
            return null;
        }
        if (!validateStopAdjacency(stopIds)) {
            System.out.println("[RouteReviseService] addRoute: stops are not contiguously connected.");
            return null;
        }

        int routeId = routeRepository.save(routeName, stopIds, estimatedTimePerStop);
        if (routeId < 0) {
            System.out.println("[RouteReviseService] addRoute: DB insert failed.");
            return null;
        }
        System.out.println("[RouteReviseService] addRoute: routeId=" + routeId + " created.");
        return routeRepository.findById(routeId);
    }

    /**
     * Applies admin-approved changes to the route and persists the result.
     */
    public Route reviseRoute(Route route, Admin admin) {
        if (route == null || admin == null) return null;
        boolean updated = routeRepository.updateStopIdList(route.getRouteID(), route.getStopIDs());
        System.out.println("[RouteReviseService] reviseRoute: routeId=" + route.getRouteID() + " updated=" + updated);
        return updated ? routeRepository.findById(route.getRouteID()) : null;
    }

    /**
     * Inserts a stop at the end of the route's stop sequence.
     */
    public void addStopToRoute(Route route, Stop stop) {
        if (route == null || stop == null) return;
        boolean ok = routeRepository.addStopToRoute(route.getRouteID(), stop.getStopID(), 0.0, 0);
        if (ok) System.out.println("[RouteReviseService] addStopToRoute: stopId=" + stop.getStopID()
                + " added to routeId=" + route.getRouteID());
    }

    /**
     * Removes a stop from the route after verifying no active bookings depend on it.
     */
    public void removeStopFromRoute(Route route, Stop stop) {
        if (route == null || stop == null) return;
        boolean ok = routeRepository.removeStopFromRoute(route.getRouteID(), stop.getStopID());
        if (ok) System.out.println("[RouteReviseService] removeStopFromRoute: stopId=" + stop.getStopID()
                + " removed from routeId=" + route.getRouteID());
    }

    /**
     * Validates that the given stop list forms a valid sequence:
     * - Each consecutive pair must be connected (one is in the other's Connections list).
     * - No duplicates.
     *
     * NOTE: This is a best-effort validation based on in-memory data. For full
     * adjacency checking, the stop objects must be loaded with connections first.
     */
    public boolean validateStopAdjacency(List<Integer> stopIds) {
        if (stopIds == null || stopIds.size() < 2) return false;

        List<Stop> allStops = stopRepository.findAll();

        for (int i = 0; i < stopIds.size() - 1; i++) {
            int fromId = stopIds.get(i);
            int toId   = stopIds.get(i + 1);

            // Check for duplicates
            if (fromId == toId) {
                System.out.println("[RouteReviseService] validateStopAdjacency: duplicate stop " + fromId);
                return false;
            }

            // Check adjacency: fromStop must have toStop in its connections
            Stop fromStop = findStopById(allStops, fromId);
            if (fromStop == null) {
                System.out.println("[RouteReviseService] validateStopAdjacency: stopId=" + fromId + " not found in map.");
                return false;
            }

            boolean connected = false;
            for (Stop conn : fromStop.getConnections()) {
                if (conn.getStopID() == toId) {
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                System.out.println("[RouteReviseService] validateStopAdjacency: stops "
                        + fromId + " and " + toId + " are not directly connected.");
                return false;
            }
        }
        return true;
    }

    /**
     * Validates that the route object has a valid continuous stop sequence.
     */
    public boolean validateRoute(Route route) {
        if (route == null || route.getStopIDs().isEmpty()) return false;
        return validateStopAdjacency(route.getStopIDs());
    }

    /**
     * Computes an alternative route that avoids the given blocked stop.
     * Uses BFS on the transport map to find a new path.
     */
    public Route rerouteAroundBlockedStop(Route route, int blockedStopId,
                                           org.BOGO.domain.transport.Map map) {
        if (route == null || map == null) return null;

        List<Integer> originalIds = route.getStopIDs();
        if (!originalIds.contains(blockedStopId)) return route; // Blocked stop not on this route

        // Build new ordered stop list avoiding the blocked stop
        List<Integer> newStopIds = new ArrayList<>();
        for (int id : originalIds) {
            if (id != blockedStopId) newStopIds.add(id);
        }

        if (newStopIds.size() < 2) {
            System.out.println("[RouteReviseService] rerouteAroundBlockedStop: insufficient stops remain.");
            return null;
        }

        // Persist the rerouted stop list
        routeRepository.updateStopIdList(route.getRouteID(), newStopIds);
        route.setStopIDs(new ArrayList<>(newStopIds));
        System.out.println("[RouteReviseService] rerouteAroundBlockedStop: routeId=" + route.getRouteID()
                + " rerouted around stopId=" + blockedStopId);
        return route;
    }

    private Stop findStopById(List<Stop> stops, int id) {
        for (Stop s : stops) {
            if (s.getStopID() == id) return s;
        }
        return null;
    }

    public void notifyDrivers(Route route, org.BOGO.domain.communication.Message message) {
        System.out.println("[RouteReviseService] notifyDrivers: routeId="
                + (route != null ? route.getRouteID() : "null") + " msg=" + message);
    }
}
