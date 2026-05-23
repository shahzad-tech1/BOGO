package org.BOGO.service;

import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.transport.Trip;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.TripRepository;

import java.util.List;

/**
 * TripService handles the full lifecycle of trips:
 * creation, progress tracking, and completion.
 */
public class TripService {

    private final TripRepository tripRepository = new TripRepository();
    private final BusRepository busRepository = new BusRepository();
    private final RouteRepository routeRepository = new RouteRepository();

    /**
     * Returns all currently active (IN_PROGRESS) trips with their associated bus data.
     */
    public List<Trip> getAllActiveTrips() {
        return tripRepository.findLiveTripsWithBuses();
    }

    /**
     * Returns the active trip for the given driver.
     */
    public Trip getTripForDriver(int driverId) {
        return tripRepository.findActiveByDriverId(driverId);
    }

    /**
     * Creates a new trip for the given route and bus, assigned to the driver.
     * Returns the created Trip object, or null on failure.
     */
    public Trip createTrip(int routeId, int busId, int driverId) {
        if (!routeRepository.routeExists(routeId)) {
            System.out.println("[TripService] createTrip: routeId=" + routeId + " not found.");
            return null;
        }
        int tripId = tripRepository.save(routeId, busId, driverId);
        if (tripId < 0) {
            System.out.println("[TripService] createTrip: DB insert failed.");
            return null;
        }
        System.out.println("[TripService] createTrip: tripId=" + tripId + " created.");
        return tripRepository.findById(tripId);
    }

    /**
     * Advances the simulation state for a trip by one tick.
     * Called by BusSimulationEngine on each scheduler tick.
     */
    public boolean updateTripProgress(int tripId, int currentStopIndex, double simulatedProgress) {
        return tripRepository.updateProgress(tripId, currentStopIndex, simulatedProgress);
    }

    /**
     * Marks a trip as COMPLETED and clears the arrival time.
     */
    public boolean completeTrip(int tripId) {
        return tripRepository.updateStatus(tripId, "COMPLETED");
    }

    /**
     * Returns the route associated with a specific trip.
     */
    public Route getRouteForTrip(Trip trip) {
        if (trip == null) return null;
        return routeRepository.findById(trip.getRouteID());
    }

    /**
     * Returns the ordered list of stops for a trip's route.
     */
    public List<Stop> getStopsForTrip(Trip trip) {
        if (trip == null) return List.of();
        Route route = routeRepository.findById(trip.getRouteID());
        if (route == null) return List.of();
        List<Stop> allStops = new org.BOGO.repository.StopRepository().findAll();
        List<Stop> routeStops = new java.util.ArrayList<>();
        for (int id : route.getStopIDs()) {
            for (Stop s : allStops) {
                if (s.getStopID() == id) {
                    routeStops.add(s);
                    break;
                }
            }
        }
        return routeStops;
    }
}
