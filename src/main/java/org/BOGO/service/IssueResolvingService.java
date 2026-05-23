package org.BOGO.service;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.communication.Alert;
import org.BOGO.domain.communication.Message;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.BusStatus;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.repository.AlertRepository;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.NotificationRepository;
import org.BOGO.repository.TripRepository;

public class IssueResolvingService {

    private final BusRepository  busRepository = new BusRepository();
    private final TripRepository tripRepository = new TripRepository();
    private final AlertRepository alertRepository = new AlertRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();


    /**
     * Retrieves the full incident message/log for the given incident ID.
     */
    public Message getIncident(String incidentID) {
        Alert alert = alertRepository.findById(Integer.parseInt(incidentID));
        return alert == null ? null : new Message(alert.getAlertId(), alert.getMessage());
    }

    public boolean resolveBusDown(int alertId, int adminId, int oldBusId, int driverUserId) {
        Bus replacement = busRepository.findFirstByStatus(BusStatus.AVAILABLE);
        if (replacement == null) {
            return false;
        }
        try (java.sql.Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                busRepository.updateStatus(conn, oldBusId, BusStatus.BUS_DOWN);
                tripRepository.swapBus(conn, oldBusId, replacement.getBusID());
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO RESOLUTIONS (AlertId, AdminId, NewBusId, ResolvedAt) VALUES (?, ?, ?, GETDATE())")) {
                    ps.setInt(1, alertId);
                    ps.setInt(2, adminId);
                    ps.setInt(3, replacement.getBusID());
                    ps.executeUpdate();
                }
                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE ALERTS SET Status = 'RESOLVED' WHERE AlertId = ?")) {
                    ps.setInt(1, alertId);
                    ps.executeUpdate();
                }
                conn.commit();
                notificationRepository.save(driverUserId, "Your new bus is " + replacement.getBusID());
                return true;
            } catch (java.sql.SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[IssueResolvingService] resolveBusDown failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Records the dispatch of a backup driver against the incident and
     * notifies all relevant parties.
     */
    public void dispatchBackup(Admin admin, Driver backupDriver, String incidentID) {}

    /**
     * Sends a confirmation message to the affected driver that help is on the way.
     */
    public void notifyAffectedDriver(Driver driver, Message message) {}

    /**
     * Broadcasts a rerouting notification to all affected passengers.
     */
    public void notifyPassengers(Bus bus, Message message) {}

    /**
     * Closes the incident, logs resolution details, and restores normal
     * service status for the route.
     */
    public void resolveIncident(String incidentID) {}

    /**
     * Transfers the active route and pending stop list from one driver to another.
     */
    public void transferRoute(Driver fromDriver, Driver toDriver) {}
}
