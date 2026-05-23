package org.BOGO.controller;

import org.BOGO.domain.transport.Map;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.repository.MapRepository;
import org.BOGO.repository.StopRepository;
import org.BOGO.service.ViewMapService;

import java.util.List;

/**
 * MapController is the central gateway for all map-related operations.
 *
 * Used by:
 *  - AdminShell: full map view + stop/route marking
 *  - DriverShell: route-specific map filtered by assigned route
 *  - PassengerShell: read-only map for stop selection
 */
public class MapController {

    private final MapRepository mapRepository = new MapRepository();
    private final StopRepository stopRepository = new StopRepository();
    private final ViewMapService viewMapService = new ViewMapService();

    /** Singleton transport map (refreshed on demand) */
    private Map cachedMap;

    // -----------------------------------------------------------------------
    // Map Loading
    // -----------------------------------------------------------------------

    /**
     * Returns the full transport map (all stops and routes).
     * Builds from DB on first call; subsequent calls use the cached instance.
     */
    public Map getFullMap() {
        if (cachedMap == null) {
            cachedMap = mapRepository.buildMap();
        }
        return cachedMap;
    }

    /**
     * Forces a refresh of the transport map from the database.
     */
    public Map refreshMap() {
        cachedMap = mapRepository.refreshMap();
        return cachedMap;
    }

    /**
     * Returns a partial map filtered to only the stops on a specific route.
     * Used by the Driver UI to show only the driver's assigned route.
     */
    public Map getMapForDriver(int assignedRouteId) {
        return mapRepository.buildMapForRoute(assignedRouteId);
    }

    // -----------------------------------------------------------------------
    // Stop Operations
    // -----------------------------------------------------------------------

    /**
     * Returns all stops on the map for rendering.
     */
    public List<Stop> getAllStops() {
        return viewMapService.getStopsOnMap();
    }

    /**
     * Returns details for a single stop.
     */
    public Stop getStopDetails(int stopId) {
        return stopRepository.findById(stopId);
    }

    /**
     * Marks a stop as inactive (blocked/out-of-service).
     * Drivers can report blocked stops; admins confirm deactivation.
     * Updates the DB — takes effect on next map refresh.
     */
    public boolean markStopInactive(int stopId) {
        String sql = "UPDATE STOPS SET IsActive = 0 WHERE StopId = ?";
        return executeUpdate(sql, stopId);
    }

    /**
     * Marks a previously inactive stop as active again.
     */
    public boolean markStopActive(int stopId) {
        String sql = "UPDATE STOPS SET IsActive = 1 WHERE StopId = ?";
        return executeUpdate(sql, stopId);
    }

    /**
     * Returns whether a stop is currently active.
     */
    public boolean isStopActive(int stopId) {
        Stop stop = stopRepository.findById(stopId);
        return stop != null && stop.isActive();
    }

    // -----------------------------------------------------------------------
    // Route Operations
    // -----------------------------------------------------------------------

    /**
     * Returns all active routes for map overlay rendering.
     */
    public List<Route> getActiveRoutes() {
        return viewMapService.getActiveRoutes();
    }

    /**
     * Deactivates a route (e.g., during major disruptions).
     */
    public boolean blockRoute(int routeId) {
        org.BOGO.repository.RouteRepository routeRepo = new org.BOGO.repository.RouteRepository();
        return routeRepo.setActive(routeId, false);
    }

    /**
     * Re-activates a blocked route.
     */
    public boolean unblockRoute(int routeId) {
        org.BOGO.repository.RouteRepository routeRepo = new org.BOGO.repository.RouteRepository();
        return routeRepo.setActive(routeId, true);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private boolean executeUpdate(String sql, int stopId) {
        try (java.sql.Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stopId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                cachedMap = null; // Invalidate cache so next getFullMap() re-fetches
            }
            return updated;
        } catch (java.sql.SQLException e) {
            System.err.println("[MapController] executeUpdate failed: " + e.getMessage());
            return false;
        }
    }
}
