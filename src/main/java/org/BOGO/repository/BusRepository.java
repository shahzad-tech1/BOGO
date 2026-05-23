package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.BusStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusRepository {
    public Bus findById(int busID) {
        String sql = "SELECT BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity FROM BUS WHERE BusId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBus(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    public Bus findFirstByStatus(BusStatus status) {
        String sql = "SELECT TOP 1 BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity FROM BUS WHERE BusStatus = ? ORDER BY BusId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBus(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] findFirstByStatus failed: " + e.getMessage());
        }
        return null;
    }

    public List<Bus> findAll() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT BusId, BusCompany, Registration, RegistrationYear, BusStatus, Capacity FROM BUS ORDER BY BusId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                buses.add(mapBus(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] findAll failed: " + e.getMessage());
        }
        return buses;
    }

    public int save(Bus bus) {
        String sql = "INSERT INTO BUS (BusCompany, Registration, RegistrationYear, BusStatus, Capacity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bus.getBusCompany());
            ps.setString(2, bus.getRegistration());
            ps.setInt(3, bus.getRegistrationYear());
            ps.setString(4, bus.getBusStatus().name());
            ps.setInt(5, bus.getCapacity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BusRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateStatus(int busID, BusStatus status) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return updateStatus(conn, busID, status);
        } catch (SQLException e) {
            System.err.println("[BusRepository] updateStatus failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(Connection conn, int busID, BusStatus status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE BUS SET BusStatus = ? WHERE BusId = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, busID);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignDriver(int busID, int driverID) {
        String sql = "UPDATE DRIVER SET AssignedBusId = ? WHERE UserId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busID);
            ps.setInt(2, driverID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BusRepository] assignDriver failed: " + e.getMessage());
            return false;
        }
    }

    public boolean assignRoute(int busID, int routeID) {
        return true; // Routes are assigned via DRIVER.AssignedRouteId
    }

    /**
     * Updates the simulated position of a bus on the map.
     */
    public boolean updateLocation(int busID, double latitude, double longitude) {
        String sql = "UPDATE BUS SET CurrentLatitude = ?, CurrentLongitude = ? WHERE BusId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            ps.setInt(3, busID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BusRepository] updateLocation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the health metrics of a bus.
     */
    public boolean updateHealthMetrics(int busID, double tyreHealth, double engineHealth, double chassisHealth) {
        String sql = "UPDATE BUS SET TyreHealth = ?, EngineHealth = ?, ChassisHealth = ? WHERE BusId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, tyreHealth);
            ps.setDouble(2, engineHealth);
            ps.setDouble(3, chassisHealth);
            ps.setInt(4, busID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BusRepository] updateHealthMetrics failed: " + e.getMessage());
            return false;
        }
    }

    private Bus mapBus(ResultSet rs) throws SQLException {
        String rawStatus = rs.getString("BusStatus");
        BusStatus status;
        try {
            status = rawStatus == null ? BusStatus.AVAILABLE : BusStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            status = BusStatus.AVAILABLE;
        }
        Bus bus = new Bus(
                rs.getInt("BusId"),
                rs.getString("BusCompany"),
                rs.getString("Registration"),
                rs.getInt("RegistrationYear"),
                status,
                rs.getInt("Capacity")
        );
        // Try to read health and location columns (may not exist if schema_extension not yet run)
        try {
            bus.setTyreHealth(rs.getDouble("TyreHealth"));
            bus.setEngineHealth(rs.getDouble("EngineHealth"));
            bus.setChassisHealth(rs.getDouble("ChassisHealth"));
        } catch (SQLException ignored) {
            // Columns not yet added — safe default values already set in Bus constructor
        }
        try {
            double lat = rs.getDouble("CurrentLatitude");
            double lon = rs.getDouble("CurrentLongitude");
            bus.setLocation(new org.BOGO.domain.transport.Location(lat, lon));
        } catch (SQLException ignored) {}
        return bus;
    }
}
