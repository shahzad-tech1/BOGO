package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteRepository {
    public boolean routeExists(int routeID) {
        String sql = "SELECT COUNT(*) FROM ROUTEE WHERE RouteId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] routeExists failed: " + e.getMessage());
            return false;
        }
    }

    public List<Route> findAll() {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT RouteId, Active, Stop_IDs FROM ROUTEE ORDER BY RouteId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Route route = new Route(rs.getInt("RouteId"));
                route.setStopIDs(parseIds(rs.getString("Stop_IDs")));
                routes.add(route);
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] findAll failed: " + e.getMessage());
        }
        return routes;
    }

    public boolean addStopToRoute(int routeID, int stopID, double price, int sequenceNumber) {
        Route route = findById(routeID);
        if (route == null) {
            return false;
        }
        ArrayList<Integer> ids = route.getStopIDs();
        if (!ids.contains(stopID)) {
            ids.add(stopID);
        }
        return updateStopIds(routeID, ids);
    }

    public boolean removeStopFromRoute(int routeID, int stopID) {
        Route route = findById(routeID);
        if (route == null) {
            return false;
        }
        ArrayList<Integer> ids = route.getStopIDs();
        ids.remove(Integer.valueOf(stopID));
        return updateStopIds(routeID, ids);
    }

    public boolean assignBusToRoute(int busID, int routeID) {
        return routeExists(routeID);
    }

    public Route findById(int routeID) {
        String sql = "SELECT RouteId, Stop_IDs FROM ROUTEE WHERE RouteId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Route route = new Route(rs.getInt("RouteId"));
                    route.setStopIDs(parseIds(rs.getString("Stop_IDs")));
                    return route;
                }
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    private boolean updateStopIds(int routeID, List<Integer> stopIds) {
        String sql = "UPDATE ROUTEE SET Stop_IDs = ? WHERE RouteId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, joinIds(stopIds));
            ps.setInt(2, routeID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteRepository] updateStopIds failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Finds all routes and wires their Stop objects from the provided stop list.
     * Used by MapRepository to build the full transport map.
     */
    public List<Route> findAllWithStops(List<Stop> allStops) {
        List<Route> routes = findAllFull();
        for (Route route : routes) {
            ArrayList<Stop> orderedStops = new ArrayList<>();
            for (int id : route.getStopIDs()) {
                for (Stop s : allStops) {
                    if (s.getStopID() == id) {
                        orderedStops.add(s);
                        break;
                    }
                }
            }
            route.setStops(orderedStops);
        }
        return routes;
    }

    /**
     * Finds all routes including RouteName and EstimatedTimePerStop (after schema extension).
     */
    public List<Route> findAllFull() {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT RouteId, Active, Stop_IDs, RouteName, EstimatedTimePerStop FROM ROUTEE ORDER BY RouteId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Route route = new Route(rs.getInt("RouteId"));
                route.setStopIDs(parseIds(rs.getString("Stop_IDs")));
                // RouteName and EstimatedTimePerStop may not exist until schema_extension.sql is run
                try {
                    route.setRouteName(rs.getString("RouteName"));
                    route.setEstimatedTimePerStop(rs.getInt("EstimatedTimePerStop"));
                } catch (SQLException ignored) {
                    // Columns not yet added — safe to ignore during transition
                }
                routes.add(route);
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] findAllFull failed: " + e.getMessage());
        }
        return routes;
    }

    /**
     * Inserts a new route into ROUTEE.
     * Returns the generated RouteId, or -1 on failure.
     */
    public int save(String routeName, List<Integer> stopIds, int estimatedTimePerStop) {
        String sql = "INSERT INTO ROUTEE (Active, Stop_IDs, RouteName, EstimatedTimePerStop) VALUES (1, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, joinIds(stopIds));
            ps.setString(2, routeName);
            ps.setInt(3, estimatedTimePerStop);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[RouteRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Sets a route active or inactive.
     */
    public boolean setActive(int routeId, boolean active) {
        String sql = "UPDATE ROUTEE SET Active = ? WHERE RouteId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, routeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RouteRepository] setActive failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the Stop_IDs serialized string for a route (used by rerouting algorithm).
     */
    public boolean updateStopIdList(int routeId, List<Integer> stopIds) {
        return updateStopIds(routeId, stopIds);
    }

    static ArrayList<Integer> parseIds(String raw) {
        ArrayList<Integer> ids = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        for (String token : raw.split(",")) {
            try {
                ids.add(Integer.parseInt(token.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    static String joinIds(List<Integer> ids) {
        StringBuilder builder = new StringBuilder();
        for (Integer id : ids) {
            if (id == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }
}
