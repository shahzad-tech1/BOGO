package org.BOGO.map;

import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.transport.Trip;
import org.BOGO.repository.RouteRepository;
import org.BOGO.repository.StopRepository;
import org.BOGO.repository.TripRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads all map data from the DB in one blocking call.
 * Call loadAll() on a BACKGROUND THREAD — never on the FX thread.
 * Cache the result and never call it during rendering.
 */
public class MapDataLoader {

    private final StopRepository  stopRepository;
    private final RouteRepository routeRepository;
    private final TripRepository  tripRepository;

    private List<Stop>  allStops;
    private List<Route> allRoutes;
    private List<Trip>  activeTrips;
    private boolean loaded = false;

    public MapDataLoader(StopRepository stopRepo, RouteRepository routeRepo, TripRepository tripRepo) {
        this.stopRepository  = stopRepo;
        this.routeRepository = routeRepo;
        this.tripRepository  = tripRepo;
    }

    /**
     * Blocking — call from a background Task.
     * Also resolves route.getStops() so the simulation engine can interpolate positions.
     */
    public void loadAll() {
        this.allStops   = stopRepository.findAll();
        this.allRoutes  = routeRepository.findAll();
        this.activeTrips = tripRepository.findActiveTrips();

        // Build a quick id→stop lookup for route resolution
        java.util.Map<Integer, Stop> stopById = new java.util.HashMap<>();
        for (Stop s : allStops) stopById.put(s.getStopID(), s);

        // Resolve route stops so engine can interpolate positions
        for (Route r : allRoutes) {
            if (r.getStops() == null || r.getStops().isEmpty()) {
                // Route.Stop_IDs is a comma-separated string of stop IDs
                // RouteRepository already resolves this — if not, do it here
                List<Stop> routeStops = new ArrayList<>();
                for (int id : r.getStopIDs()) {
                    Stop s = stopById.get(id);
                    if (s != null) routeStops.add(s);
                }
                r.setStops(new ArrayList<>(routeStops));
            }
        }

        // Resolve trip → route so engine can read route.getStops()
        java.util.Map<Integer, Route> routeById = new java.util.HashMap<>();
        for (Route r : allRoutes) routeById.put(r.getRouteID(), r);
        for (Trip t : activeTrips) {
            if (t.getRoute() == null) {
                Route r = routeById.get(t.getRouteID());
                if (r != null) t.setRoute(r);
            }
        }
        this.loaded = true;
    }

    /** Lightweight refresh — call periodically if needed (still background thread). */
    public void refreshTrips() {
        this.activeTrips = tripRepository.findActiveTrips();
        java.util.Map<Integer, Route> routeById = new java.util.HashMap<>();
        for (Route r : allRoutes) routeById.put(r.getRouteID(), r);
        for (Trip t : activeTrips) {
            if (t.getRoute() == null) {
                Route r = routeById.get(t.getRouteID());
                if (r != null) t.setRoute(r);
            }
        }
    }

    public List<Stop>  getAllStops()    { return allStops;    }
    public List<Route> getAllRoutes()   { return allRoutes;   }
    public List<Trip>  getActiveTrips() { return activeTrips; }
    public boolean     isLoaded()       { return loaded;      }
}
