package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Location;
import org.BOGO.domain.transport.Stop;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StopRepository {
    public List<Stop> findAll() {
        HashMap<Integer, Stop> byId = new HashMap<>();
        String sql = """
                SELECT s.StopId, s.StopName, s.Connections, l.Longitude, l.Latitude
                FROM STOPS s
                LEFT JOIN LOCATIONN l ON l.LocationId = s.LocationId
                ORDER BY s.StopId
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            HashMap<Integer, String> rawConnections = new HashMap<>();
            while (rs.next()) {
                Stop stop = mapStopShell(rs);
                byId.put(stop.getStopID(), stop);
                rawConnections.put(stop.getStopID(), rs.getString("Connections"));
            }
            for (java.util.Map.Entry<Integer, String> entry : rawConnections.entrySet()) {
                Stop stop = byId.get(entry.getKey());
                for (Integer connectedId : parseIds(entry.getValue())) {
                    Stop connected = byId.get(connectedId);
                    if (connected != null) {
                        stop.addConnection(connected, 1.0, 0);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] findAll failed: " + e.getMessage());
        }
        return new ArrayList<>(byId.values());
    }

    public List<Stop> findByRouteId(int routeID) {
        RouteRepository routeRepository = new RouteRepository();
        List<Integer> ids = routeRepository.findById(routeID) == null
                ? List.of()
                : routeRepository.findById(routeID).getStopIDs();
        List<Stop> stops = new ArrayList<>();
        for (Integer id : ids) {
            Stop stop = findById(id);
            if (stop != null) {
                stops.add(stop);
            }
        }
        return stops;
    }

    public Stop findById(int stopID) {
        String sql = """
                SELECT s.StopId, s.StopName, s.Connections, l.Longitude, l.Latitude
                FROM STOPS s
                LEFT JOIN LOCATIONN l ON l.LocationId = s.LocationId
                WHERE s.StopId = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stopID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStopShell(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    public boolean existsByNameOrLocation(String stopName, int locationX, int locationY) {
        String sql = """
                SELECT COUNT(*)
                FROM STOPS s
                LEFT JOIN LOCATIONN l ON l.LocationId = s.LocationId
                WHERE s.StopName = ? OR (l.Longitude = ? AND l.Latitude = ?)
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stopName);
            ps.setInt(2, locationX);
            ps.setInt(3, locationY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] existsByNameOrLocation failed: " + e.getMessage());
            return false;
        }
    }

    public boolean hasActiveBookings(int stopID) {
        String sql = "SELECT COUNT(*) FROM BOOKING WHERE Active = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[StopRepository] hasActiveBookings failed: " + e.getMessage());
            return false;
        }
    }

    public int save(Stop stop, double latitude, double longitude) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int locationId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO LOCATIONN (Longitude, Latitude) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDouble(1, longitude);
                    ps.setDouble(2, latitude);
                    ps.executeUpdate();
                    locationId = generatedKey(ps);
                }
                int stopId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO STOPS (StopName, LocationId, Connections) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, stop.getStopName());
                    ps.setInt(2, locationId);
                    ps.setString(3, serializeConnections(stop));
                    ps.executeUpdate();
                    stopId = generatedKey(ps);
                }
                conn.commit();
                return stopId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] save failed: " + e.getMessage());
            return -1;
        }
    }

    /** Appends connectedStopId to the Connections column of an existing stop (bidirectional link). */
    public void addConnection(int stopId, int connectedStopId) {
        String selectSql = "SELECT Connections FROM STOPS WHERE StopId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, stopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String existing = rs.getString("Connections");
                    java.util.List<Integer> ids = parseIds(existing);
                    if (!ids.contains(connectedStopId)) {
                        ids.add(connectedStopId);
                        updateConnections(stopId, ids);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[StopRepository] addConnection failed: " + e.getMessage());
        }
    }

    public boolean update(int stopID, String newName, int locationX, int locationY, boolean active) {
        String sql = "UPDATE STOPS SET StopName = ? WHERE StopId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, stopID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StopRepository] update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateConnections(int stopID, List<Integer> connectionIds) {
        String sql = "UPDATE STOPS SET Connections = ? WHERE StopId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, RouteRepository.joinIds(connectionIds));
            ps.setInt(2, stopID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StopRepository] updateConnections failed: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int stopID) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM STOPS WHERE StopId = ?")) {
            ps.setInt(1, stopID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StopRepository] delete failed: " + e.getMessage());
            return false;
        }
    }

    private Stop mapStopShell(ResultSet rs) throws SQLException {
        Stop stop = new Stop();
        stop.initializeStop(
                rs.getInt("StopId"),
                rs.getString("StopName"),
                new Location(rs.getDouble("Latitude"), rs.getDouble("Longitude"))
        );
        return stop;
    }

    private ArrayList<Integer> parseIds(String raw) {
        return RouteRepository.parseIds(raw);
    }

    private String serializeConnections(Stop stop) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Stop connected : stop.getConnections()) {
            ids.add(connected.getStopID());
        }
        return RouteRepository.joinIds(ids);
    }

    private int generatedKey(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No generated key returned.");
    }
}
