package org.BOGO.service;

import org.BOGO.domain.communication.Message;
import org.BOGO.domain.communication.Alert;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Location;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.repository.AlertRepository;
import org.BOGO.repository.BusRepository;

public class IssueReportingService {

    private final BusRepository  busRepository = new BusRepository();
    private final AlertRepository alertRepository = new AlertRepository();



    /**
     * Records an incident report for the driver at the given location.
     * Returns the generated unique incident ID string.
     */
    public String reportIssue(Driver driver, String type, String details, Location location) {
        if (driver == null) {
            return null;
        }
        Alert alert = new Alert();
        alert.setSenderDriverId(driver.getUserID());
        alert.setAlertType(type);
        alert.setPriority("HIGH");
        alert.setMessage(details);
        alert.setStatus("OPEN");
        int id = alertRepository.save(alert);
        return id < 0 ? null : String.valueOf(id);
    }

    /**
     * Updates the bus status to BUS_DOWN / DRIVER_DOWN and suspends
     * further stop assignments.
     */
    public void flagBus(Bus bus) {
        if (bus != null) {
            busRepository.updateStatus(bus.getBusID(), org.BOGO.domain.transport.BusStatus.BUS_DOWN);
        }
    }

    /**
     * Sends an immediate alert to the admin portal with incident details.
     */
    public void notifyAdmin(Admin admin, String incidentID) {}

    /**
     * Sends a disruption notification to all passengers with active bookings
     * on the affected bus.
     */
    public void notifyPassengers(Bus bus, Message message) {}
}
