package org.BOGO.service;

import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.BusStatus;
import org.BOGO.repository.BusRepository;

import java.util.List;

/**
 * BusService handles bus lifecycle: addition, status management, and health reporting.
 * Renamed from BusServices.java to BusService.java — ResourceController uses BusServices.
 */
public class BusServices {

    private final BusRepository busRepository = new BusRepository();

    /**
     * Adds a new bus to the fleet.
     * Returns the generated BusId, or -1 on failure.
     */
    public int addBus(String company, String registration, int registrationYear, int capacity) {
        Bus bus = new Bus(0, company, registration, registrationYear, BusStatus.AVAILABLE, capacity);
        int id = busRepository.save(bus);
        System.out.println("[BusServices] addBus: " + registration + " id=" + id);
        return id;
    }

    /**
     * Returns all buses in the fleet.
     */
    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    /**
     * Updates the status of a bus.
     */
    public boolean updateBusStatus(int busId, BusStatus status) {
        return busRepository.updateStatus(busId, status);
    }

    /**
     * Returns the first available bus (for quick assignment).
     */
    public Bus getAvailableBus() {
        return busRepository.findFirstByStatus(BusStatus.AVAILABLE);
    }

    /**
     * Updates the health metrics of a bus (called by driver after inspection).
     */
    public boolean updateHealthMetrics(int busId, double tyreHealth, double engineHealth, double chassisHealth) {
        return busRepository.updateHealthMetrics(busId, tyreHealth, engineHealth, chassisHealth);
    }

    /**
     * Returns a bus by its ID.
     */
    public Bus getBusById(int busId) {
        return busRepository.findById(busId);
    }
}
