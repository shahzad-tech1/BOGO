package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.BusStatus;
import org.BOGO.domain.transport.Trip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {

    public Trip findById(int tripId) {
        String sql = "SELECT TripId, RouteId, BusId, DriverId, DepartureTime, ArrivalTime, " +
                     "CurrentStopIndex, SimulatedProgress, TripStatus FROM TRIP WHERE TripId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTrip(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TripRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all trips with status IN_PROGRESS.
     */
    public List<Trip> findAllActive() {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT TripId, RouteId, BusId, DriverId, DepartureTime, ArrivalTime, " +
                     "CurrentStopIndex, SimulatedProgress, TripStatus FROM TRIP WHERE TripStatus = 'IN_PROGRESS'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trips.add(mapTrip(rs));
            }
        } catch (SQLException e) {
            System.err.println("[TripRepository] findAllActive failed: " + e.getMessage());
        }
        return trips;
    }

    /** Alias used by MapDataLoader (matches the spec method name). */
    public List<Trip> findActiveTrips() { return findAllActive(); }


    /**
     * Returns all trips with their associated bus data.
     */
    public List<Trip> findLiveTripsWithBuses() {
        List<Trip> trips = new ArrayList<>();
        String sql = """
                SELECT t.TripId, t.RouteId, t.BusId, t.DriverId, t.DepartureTime, t.ArrivalTime,
                       t.CurrentStopIndex, t.SimulatedProgress, t.TripStatus,
                       b.BusCompany, b.Registration, b.RegistrationYear, b.BusStatus, b.Capacity
                FROM TRIP t
                JOIN BUS b ON b.BusId = t.BusId
                WHERE t.TripStatus = 'IN_PROGRESS'
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Trip trip = mapTrip(rs);
                String rawStatus = rs.getString("BusStatus");
                BusStatus busStatus;
                try {
                    busStatus = rawStatus == null ? BusStatus.AVAILABLE : BusStatus.valueOf(rawStatus.trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    busStatus = BusStatus.AVAILABLE;
                }
                trip.setBus(new Bus(
                        rs.getInt("BusId"),
                        rs.getString("BusCompany"),
                        rs.getString("Registration"),
                        rs.getInt("RegistrationYear"),
                        busStatus,
                        rs.getInt("Capacity")
                ));
                trips.add(trip);
            }
        } catch (SQLException e) {
            System.err.println("[TripRepository] findLiveTripsWithBuses failed: " + e.getMessage());
        }
        return trips;
    }

    /**
     * Finds the active trip for a specific driver.
     */
    public Trip findActiveByDriverId(int driverId) {
        String sql = "SELECT TOP 1 TripId, RouteId, BusId, DriverId, DepartureTime, ArrivalTime, " +
                     "CurrentStopIndex, SimulatedProgress, TripStatus FROM TRIP " +
                     "WHERE DriverId = ? AND TripStatus = 'IN_PROGRESS'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTrip(rs);
            }
        } catch (SQLException e) {
            System.err.println("[TripRepository] findActiveByDriverId failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Inserts a new trip and returns its generated ID.
     */
    public int save(int routeId, int busId, int driverId) {
        String sql = "INSERT INTO TRIP (RouteId, BusId, DriverId, DepartureTime, ArrivalTime, " +
                     "CurrentStopIndex, SimulatedProgress, TripStatus) " +
                     "VALUES (?, ?, ?, GETDATE(), DATEADD(HOUR, 2, GETDATE()), 0, 0.0, 'IN_PROGRESS')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, routeId);
            ps.setInt(2, busId);
            ps.setInt(3, driverId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[TripRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates the simulation progress for a trip (called every tick by BusSimulationEngine).
     */
    public boolean updateProgress(int tripId, int currentStopIndex, double simulatedProgress) {
        String sql = "UPDATE TRIP SET CurrentStopIndex = ?, SimulatedProgress = ? WHERE TripId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentStopIndex);
            ps.setDouble(2, simulatedProgress);
            ps.setInt(3, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TripRepository] updateProgress failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the status of a trip.
     */
    public boolean updateStatus(int tripId, String status) {
        String sql = "UPDATE TRIP SET TripStatus = ? WHERE TripId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TripRepository] updateStatus failed: " + e.getMessage());
            return false;
        }
    }

    public boolean swapBus(Connection conn, int oldBusId, int newBusId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE TRIP SET BusId = ? WHERE BusId = ?")) {
            ps.setInt(1, newBusId);
            ps.setInt(2, oldBusId);
            return ps.executeUpdate() > 0;
        }
    }

    private Trip mapTrip(ResultSet rs) throws SQLException {
        Timestamp departure = rs.getTimestamp("DepartureTime");
        Timestamp arrival = rs.getTimestamp("ArrivalTime");
        Trip trip = new Trip(
                rs.getInt("TripId"),
                rs.getInt("RouteId"),
                rs.getInt("BusId"),
                rs.getInt("DriverId"),
                departure == null ? null : departure.toLocalDateTime(),
                arrival == null ? null : arrival.toLocalDateTime()
        );
        // Read simulation fields (may not exist before schema_extension.sql)
        try {
            trip.setCurrentStopIndex(rs.getInt("CurrentStopIndex"));
            trip.setSimulatedProgress(rs.getDouble("SimulatedProgress"));
            String status = rs.getString("TripStatus");
            trip.setTripStatus(status != null ? status : "IN_PROGRESS");
        } catch (SQLException ignored) {}
        return trip;
    }
}
