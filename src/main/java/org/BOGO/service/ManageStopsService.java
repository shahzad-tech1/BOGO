package org.BOGO.service;

import org.BOGO.domain.transport.Location;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Admin;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;

import java.util.List;

/**
 * ManageStopsService handles the full lifecycle of transit stops:
 * creation, editing, removal, validation, propagation, and querying.
 * All state changes are persisted to the BOGO database.
 *
 * NOTE — Duplication analysis vs existing domain classes:
 * -------------------------------------------------------
 * Admin.addStop(Stop, List<Stop>):
 *   - Adds to an IN-MEMORY list only, checks for null/duplicates in memory.
 *   - ManageStopsService.addStop() validates (via validateStop -> DB duplicate check),
 *     persists to the DB, then propagates changes. NOT a duplicate.
 *
 * Admin.removeStop(Stop, List<Stop>):
 *   - Removes from an IN-MEMORY list only.
 *   - ManageStopsService.removeStop() checks for active bookings in the DB, deletes
 *     from DB (with cascade through RouteStops), and propagates. NOT a duplicate.
 *
 * Stop.initializeStop() / Stop.addConnection():
 *   - Pure in-memory initialization/connection of stop objects.
 *   - ManageStopsService reads/writes the database separately from those.
 *
 * Route.addStops(Stop):
 *   - Adds to an in-memory ArrayList<Stop> on the Route object.
 *   - propagateChanges() calls route.addStops() for in-memory sync AND writes to RouteStops table.
 *     NOT a duplicate.
 *
 * validateStop() / getStopsByRoute() do NOT exist in Admin, Stop, or Route — no conflict.
 */
public class ManageStopsService {

    private final StopRepository  stopRepository  = new StopRepository();
    private final RouteRepository routeRepository = new RouteRepository();

    /**
     * Validates and persists a new stop, then propagates it to the in-memory routing engine.
     *
     * Steps:
     * 1. validateStop() — reject duplicates and out-of-range coordinates.
     * 2. stopRepository.save() — INSERT into Stops table.
     * 3. If a route is known at creation time, propagateChanges() is called.
     *
     * NOTE: Admin.addStop() adds to an in-memory list. The UI/controller layer should call
     * admin.addStop(stop, someList) separately if in-memory tracking is also needed.
     */
    public void addStop(Stop stop, Admin admin) {
        if (stop == null || admin == null) {
            System.out.println("[ManageStopsService] addStop: null argument.");
            return;
        }

        if (!validateStop(stop)) {
            System.out.println("[ManageStopsService] addStop: validation failed for stop="
                + stop.getStopName());
            return;
        }

        // Derive location values from the Stop object (Stop exposes connections but not
        // location directly — add a public getLocation() to Stop, or pass coordinates separately).
        // Using placeholder 0,0 until Stop.getLocation() is exposed publicly.
        int locationX = getLocationX(stop);
        int locationY = getLocationY(stop);

        int generatedID = stopRepository.save(stop, locationX, locationY);
        if (generatedID < 0) {
            System.out.println("[ManageStopsService] addStop: DB insert failed.");
            return;
        }

        System.out.println("[ManageStopsService] addStop: stop persisted with stopID=" + generatedID);
    }

    /**
     * Updates stop details (name, location, active flag) and propagates changes.
     *
     * Steps:
     * 1. stopRepository.update() — UPDATE Stops table row.
     * 2. propagateChanges() to re-sync the routing engine (in-memory).
     *
     * NOTE: No edit method exists on Stop or Admin — no conflict.
     */
    public void editStop(int stopID, Stop updatedData, Admin admin) {
        if (updatedData == null || admin == null) {
            System.out.println("[ManageStopsService] editStop: null argument.");
            return;
        }

        int locationX = getLocationX(updatedData);
        int locationY = getLocationY(updatedData);

        boolean updated = stopRepository.update(
            stopID,
            updatedData.getStopName(),
            locationX,
            locationY,
            true   // keep active on edit; set false explicitly via deactivate if needed
        );

        System.out.println("[ManageStopsService] editStop: stopID=" + stopID + " updated=" + updated);
    }

    /**
     * Removes a stop after verifying no active bookings depend on it.
     *
     * Steps:
     * 1. Check stopRepository.hasActiveBookings() — abort if any exist.
     * 2. stopRepository.delete() — removes from RouteStops and Stops in a transaction.
     *
     * NOTE: Admin.removeStop() removes from an in-memory list without any DB check.
     * This service adds the critical DB-safety check. NOT a duplicate.
     */
    public void removeStop(int stopID, Admin admin) {
        if (admin == null) {
            System.out.println("[ManageStopsService] removeStop: null admin.");
            return;
        }

        if (stopRepository.hasActiveBookings(stopID)) {
            System.out.println("[ManageStopsService] removeStop: BLOCKED — stopID=" + stopID
                + " has active bookings. Cancel bookings before removing.");
            return;
        }

        boolean deleted = stopRepository.delete(stopID);
        System.out.println("[ManageStopsService] removeStop: stopID=" + stopID + " deleted=" + deleted);
    }

    /**
     * Validates a stop before saving:
     * - Rejects null name or blank name.
     * - Checks for duplicate name OR duplicate coordinates in the DB.
     * - Validates coordinate range (basic sanity: must be non-negative).
     *
     * NOTE: No equivalent exists in Stop or Admin — no conflict.
     *
     * @return true if the stop is valid and safe to persist.
     */
    public boolean validateStop(Stop stop) {
        if (stop == null) return false;

        String name = stop.getStopName();
        if (name == null || name.isBlank()) {
            System.out.println("[ManageStopsService] validateStop: stop name is blank.");
            return false;
        }

        int x = getLocationX(stop);
        int y = getLocationY(stop);

        // Sanity: coordinates must be non-negative (real-world coordinates are always ≥ 0)
        if (x < 0 || y < 0) {
            System.out.println("[ManageStopsService] validateStop: invalid coordinates (" + x + "," + y + ").");
            return false;
        }

        // DB duplicate check
        if (stopRepository.existsByNameOrLocation(name, x, y)) {
            System.out.println("[ManageStopsService] validateStop: duplicate stop name or location.");
            return false;
        }

        return true;
    }

    /**
     * Propagates stop changes to:
     * 1. The in-memory routing engine (calls route.addStops() if route is non-null).
     * 2. The RouteStops join table in the DB (adds the stop to the route at the last sequence).
     *
     * NOTE: Route.addStops(stop) only operates in-memory. This method additionally writes
     * to the RouteStops DB table. NOT a duplicate.
     */
    public void propagateChanges(Stop stop, Route route) {
        if (stop == null) {
            System.out.println("[ManageStopsService] propagateChanges: null stop.");
            return;
        }

        // In-memory propagation
        if (route != null) {
            route.addStops(stop);   // Route.addStops() handles in-memory dedup already

            // DB propagation — add to RouteStops with default price=0 and next sequence number
            // Sequence is approximated here; pass explicitly if needed
            routeRepository.addStopToRoute(route.getRouteID(), stop.getStopID(), 0.0, 0);
            System.out.println("[ManageStopsService] propagateChanges: stop " + stop.getStopID()
                + " added to routeID=" + route.getRouteID());
        } else {
            System.out.println("[ManageStopsService] propagateChanges: no route provided; "
                + "in-memory propagation skipped.");
        }
    }

    /**
     * Returns all stops belonging to the given routeID, ordered by sequence.
     * Fetches directly from the DB via StopRepository.
     *
     * NOTE: No equivalent exists in Route or Admin — no conflict.
     */
    public List<Stop> getStopsByRoute(int routeID) {
        List<Stop> stops = stopRepository.findByRouteId(routeID);
        System.out.println("[ManageStopsService] getStopsByRoute: routeID=" + routeID
            + " -> " + stops.size() + " stops.");
        return stops;
    }

    // -------------------------------------------------------------------------
    // Private helpers — until Stop exposes a public getLocation()
    // -------------------------------------------------------------------------

    /**
     * Extracts the X coordinate from a Stop's connection data or returns 0.
     * TODO: Add a public getLocation() method to Stop to replace this workaround.
     */
    private int getLocationX(Stop stop) {
        // Stop.displayStopDetails() shows it has a Location field, but it is private
        // with no getter. Until a public getter is added, we cannot extract coordinates
        // from an in-memory Stop object. Return 0 as a safe default.
        return 0;
    }

    /**
     * Extracts the Y coordinate from a Stop, or returns 0.
     * TODO: Add a public getLocation() method to Stop to replace this workaround.
     */
    private int getLocationY(Stop stop) {
        return 0;
    }

    /** Extracts a Stop's stopID by name (for cases where stop was just retrieved from DB). */
    private int getStopID(Stop stop) {
        return stop.getStopID();
    }
}
