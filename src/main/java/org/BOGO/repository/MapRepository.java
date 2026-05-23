package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MapRepository builds the in-memory Map domain object by aggregating
 * data from STOPS and ROUTEE tables. The map itself is not stored as a
 * single DB object — it is reconstructed on demand.
 */
public class MapRepository {

    private final StopRepository stopRepository = new StopRepository();
    private final RouteRepository routeRepository = new RouteRepository();

    /**
     * Builds the full transport map from the database.
     * Loads all stops (with connections) and all routes.
     */
    public org.BOGO.domain.transport.Map buildMap() {
        try {
            org.BOGO.domain.transport.Map map = new org.BOGO.domain.transport.Map();
            List<Stop> stops = stopRepository.findAll();
            List<Route> routes = routeRepository.findAllWithStops(stops);
            map.setStops(new java.util.ArrayList<>(stops));
            map.setRoutes(new java.util.ArrayList<>(routes));
            updateMapMetadata(stops.size(), routes.size());
            return map;
        } catch (Exception e) {
            System.err.println("[MapRepository] buildMap failed: " + e.getMessage());
            return new org.BOGO.domain.transport.Map();
        }
    }

    /**
     * Builds a partial map containing only the stops relevant to a specific route.
     */
    public org.BOGO.domain.transport.Map buildMapForRoute(int routeId) {
        try {
            org.BOGO.domain.transport.Map map = new org.BOGO.domain.transport.Map();
            List<Stop> allStops = stopRepository.findAll();
            Route route = routeRepository.findById(routeId);
            if (route == null) return map;

            // Filter stops to only those on this route
            List<Integer> stopIds = route.getStopIDs();
            List<Stop> routeStops = new ArrayList<>();
            for (Stop s : allStops) {
                if (stopIds.contains(s.getStopID())) {
                    routeStops.add(s);
                }
            }
            map.setStops(new ArrayList<>(routeStops));

            // Wire stop references into route
            List<Stop> orderedStops = new ArrayList<>();
            for (int id : stopIds) {
                for (Stop s : routeStops) {
                    if (s.getStopID() == id) {
                        orderedStops.add(s);
                        break;
                    }
                }
            }
            route.setStops(orderedStops);
            List<Route> routes = new ArrayList<>();
            routes.add(route);
            map.setRoutes(new ArrayList<>(routes));
            return map;
        } catch (Exception e) {
            System.err.println("[MapRepository] buildMapForRoute failed: " + e.getMessage());
            return new org.BOGO.domain.transport.Map();
        }
    }

    /**
     * Re-fetches data and returns an updated map (replaces the cached singleton).
     */
    public org.BOGO.domain.transport.Map refreshMap() {
        return buildMap();
    }

    private void updateMapMetadata(int totalStops, int totalRoutes) {
        String sql = "UPDATE MAP SET LastRefreshed = GETDATE(), TotalStops = ?, TotalRoutes = ? WHERE MapId = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, totalStops);
            ps.setInt(2, totalRoutes);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Non-fatal — map metadata update failure should not block the app
            System.err.println("[MapRepository] updateMapMetadata failed (non-fatal): " + e.getMessage());
        }
    }
}
