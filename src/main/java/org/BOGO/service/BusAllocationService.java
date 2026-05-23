package org.BOGO.service;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.BusStatus;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.TripRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs at JAR startup (on a background thread) to pair every unassigned
 * driver with a spare (AVAILABLE) bus, creating an IN_PROGRESS trip so
 * the driver can immediately access their bus metrics and route map.
 *
 * Called from BOGOApplication.start() — never blocks the JavaFX thread.
 */
public class BusAllocationService {

    private final BusRepository  busRepo  = new BusRepository();
    private final TripRepository tripRepo = new TripRepository();

    /**
     * Entry point: pair spare drivers with spare buses.
     * Idempotent — skips drivers who already have an active trip.
     */
    public void autoAssign() {
        try {
            List<Integer> unassignedDriverIds = findUnassignedDriverIds();
            List<Bus>     availableBuses      = findAvailableBuses();
            int           firstRouteId        = findFirstRouteId();

            int pairs = Math.min(unassignedDriverIds.size(), availableBuses.size());
            if (pairs == 0) {
                System.out.println("[BusAllocationService] No spare driver-bus pairs to assign.");
                return;
            }

            for (int i = 0; i < pairs; i++) {
                int driverId = unassignedDriverIds.get(i);
                Bus bus      = availableBuses.get(i);

                // 1. Link bus → driver in DRIVER table (AssignedBusId)
                busRepo.assignDriver(bus.getBusID(), driverId);

                // 2. Create an active TRIP so loadDriverHealthMetrics() can find it
                if (firstRouteId > 0) {
                    int tripId = tripRepo.save(firstRouteId, bus.getBusID(), driverId);
                    System.out.printf("[BusAllocationService] Driver %d → Bus %d (TripId %d)%n",
                            driverId, bus.getBusID(), tripId);
                } else {
                    System.out.printf("[BusAllocationService] Driver %d → Bus %d (no route available)%n",
                            driverId, bus.getBusID());
                }
            }
        } catch (Exception e) {
            System.err.println("[BusAllocationService] autoAssign failed: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Drivers with no IN_PROGRESS trip. */
    private List<Integer> findUnassignedDriverIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = """
                SELECT u.UserId
                FROM USERS u
                JOIN DRIVER d ON d.UserId = u.UserId
                WHERE u.UserId NOT IN (
                    SELECT DriverId FROM TRIP WHERE TripStatus = 'IN_PROGRESS'
                )
                ORDER BY u.UserId
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt("UserId"));
        } catch (SQLException e) {
            System.err.println("[BusAllocationService] findUnassignedDriverIds failed: " + e.getMessage());
        }
        return ids;
    }

    /** Buses whose BusStatus = AVAILABLE (not already in a trip). */
    private List<Bus> findAvailableBuses() {
        List<Bus> available = new ArrayList<>();
        for (Bus bus : busRepo.findAll()) {
            if (bus.getBusStatus() == BusStatus.AVAILABLE) {
                available.add(bus);
            }
        }
        return available;
    }

    /** Returns the first route ID in ROUTEE, or -1 if none exist. */
    private int findFirstRouteId() {
        String sql = "SELECT TOP 1 RouteId FROM ROUTEE WHERE Active = 1 ORDER BY RouteId";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("RouteId");
        } catch (SQLException e) {
            System.err.println("[BusAllocationService] findFirstRouteId failed: " + e.getMessage());
        }
        return -1;
    }
}
