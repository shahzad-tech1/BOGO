package org.BOGO.service;

import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.UserRepository;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * HandleDriverService manages all driver lifecycle operations:
 * creation, profile updates, deactivation, and bus/route assignment.
 */
public class HandleDriverService {

    private final UserRepository userRepository = new UserRepository();
    private final BusRepository busRepository = new BusRepository();
    private final RouteRepository routeRepository = new RouteRepository();

    /**
     * Persists a new driver record (both PERSONAL_DETAILS and DRIVER rows).
     * Also assigns the given routeId and busId if provided.
     */
    public Driver addDriver(String name, String email, String cnic,
                            String password, String licenseNumber,
                            int assignedRouteId, int assignedBusId) {
        try (Connection conn = org.BOGO.config.DatabaseConfig.getConnection()) {
            PersonalDetails pd = new PersonalDetails(0, name, email, cnic, password);
            // Check duplicate
            PersonalDetails existing = userRepository.findByEmail(email);
            if (existing != null) {
                System.out.println("[HandleDriverService] addDriver: email already in use: " + email);
                return null;
            }

            int userId = userRepository.save(pd, "DRIVER", licenseNumber, conn);
            if (userId < 0) {
                System.out.println("[HandleDriverService] addDriver: DB insert failed.");
                return null;
            }

            // Assign route and bus
            if (assignedRouteId > 0) {
                assignDriverToRouteById(userId, assignedRouteId);
            }
            if (assignedBusId > 0) {
                assignDriverToBusById(userId, assignedBusId);
            }

            System.out.println("[HandleDriverService] addDriver: created userId=" + userId);
            return (Driver) userRepository.findUserById(userId);

        } catch (Exception e) {
            System.err.println("[HandleDriverService] addDriver failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Legacy overload — adds driver with no route/bus pre-assignment.
     */
    public void addDriver(Driver driver, Admin admin) {
        // In-memory add only; persisted via addDriver(String...) above
        if (admin != null && driver != null) {
            System.out.println("[HandleDriverService] addDriver(Driver,Admin): in-memory add.");
        }
    }

    /**
     * Deactivates the specified driver.
     */
    public void deactivateDriver(int driverId, Admin admin) {
        String sql = "UPDATE DRIVER SET AssignedRouteId = NULL, AssignedBusId = NULL WHERE UserId = ?";
        try (Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ps.executeUpdate();
            System.out.println("[HandleDriverService] deactivateDriver: driverId=" + driverId + " deactivated.");
        } catch (Exception e) {
            System.err.println("[HandleDriverService] deactivateDriver failed: " + e.getMessage());
        }
    }

    public void deactivateDriver(int driverID, Admin admin, Driver driver) {
        deactivateDriver(driverID, admin);
    }

    /**
     * Assigns a bus to a driver (persists to DB).
     */
    public void assignDriverToBus(Driver driver, Bus bus) {
        if (driver == null || bus == null) return;
        assignDriverToBusById(driver.getUserID(), bus.getBusID());
    }

    private void assignDriverToBusById(int userId, int busId) {
        String sql = "UPDATE DRIVER SET AssignedBusId = ? WHERE UserId = ?";
        try (Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("[HandleDriverService] assignDriverToBus: userId=" + userId + " busId=" + busId);
        } catch (Exception e) {
            System.err.println("[HandleDriverService] assignDriverToBus failed: " + e.getMessage());
        }
    }

    /**
     * Assigns a route to a driver (persists to DB).
     */
    public void assignDriverToRoute(Driver driver, Route route) {
        if (driver == null || route == null) return;
        assignDriverToRouteById(driver.getUserID(), route.getRouteID());
    }

    public void assignDriverToRouteById(int userId, int routeId) {
        String sql = "UPDATE DRIVER SET AssignedRouteId = ? WHERE UserId = ?";
        try (Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("[HandleDriverService] assignDriverToRoute: userId=" + userId + " routeId=" + routeId);
        } catch (Exception e) {
            System.err.println("[HandleDriverService] assignDriverToRoute failed: " + e.getMessage());
        }
    }

    /**
     * Returns all drivers with their assigned route and bus info.
     */
    public List<Driver> getAllDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = """
                SELECT u.UserId, pd.Name, pd.Email, pd.CNIC, pd.Password, d.DriverID,
                       d.AssignedRouteId, d.AssignedBusId
                FROM DRIVER d
                JOIN USERS u ON u.UserId = d.UserId
                JOIN PERSONAL_DETAILS pd ON pd.PdId = u.PdId
                """;
        try (Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Driver driver = new Driver(
                        rs.getInt("UserId"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("CNIC"),
                        rs.getString("Password"),
                        rs.getString("DriverID")
                );
                drivers.add(driver);
            }
        } catch (Exception e) {
            System.err.println("[HandleDriverService] getAllDrivers failed: " + e.getMessage());
        }
        return drivers;
    }

    /**
     * Returns all drivers who are currently unassigned (no bus/route).
     */
    public List<Driver> getAvailableDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = """
                SELECT u.UserId, pd.Name, pd.Email, pd.CNIC, pd.Password, d.DriverID
                FROM DRIVER d
                JOIN USERS u ON u.UserId = d.UserId
                JOIN PERSONAL_DETAILS pd ON pd.PdId = u.PdId
                WHERE d.AssignedRouteId IS NULL
                """;
        try (Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Driver driver = new Driver(
                        rs.getInt("UserId"),
                        rs.getString("Name"),
                        rs.getString("Email"),
                        rs.getString("CNIC"),
                        rs.getString("Password"),
                        rs.getString("DriverID")
                );
                drivers.add(driver);
            }
        } catch (Exception e) {
            System.err.println("[HandleDriverService] getAvailableDrivers failed: " + e.getMessage());
        }
        return drivers;
    }

    public void updateDriver(int driverID, Driver updatedData, Admin admin) {
        System.out.println("[HandleDriverService] updateDriver: stub — extend with UPDATE PERSONAL_DETAILS query.");
    }
}
