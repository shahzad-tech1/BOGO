package org.BOGO.map;

import org.BOGO.domain.transport.Stop;
import java.util.List;

/**
 * Converts raw GPS lat/lon coordinates to canvas pixel coordinates.
 * Call calibrate() ONCE after loading stops from DB; toPixelX/Y are then O(1).
 *
 * Rule: longitude → X (east-west), latitude → Y (north-south, inverted so north = top).
 */
public class CoordinateNormalizer {

    private double minLat, maxLat, minLon, maxLon;
    private double canvasWidth, canvasHeight;
    private static final double PADDING = 0.10; // 10% padding on all sides

    /**
     * Calibrate from the full list of stops.
     * Filters out 0,0 coordinates (placeholder / null data).
     */
    public void calibrate(List<Stop> allStops, double canvasWidth, double canvasHeight) {
        if (allStops == null || allStops.isEmpty()) return;
        this.canvasWidth  = canvasWidth;
        this.canvasHeight = canvasHeight;

        List<Stop> valid = allStops.stream()
                .filter(s -> s.getLocation() != null
                          && s.getLocation().getLatitude()  != 0
                          && s.getLocation().getLongitude() != 0)
                .toList();
        if (valid.isEmpty()) return;

        minLat = valid.stream().mapToDouble(s -> s.getLocation().getLatitude()).min().orElse(0);
        maxLat = valid.stream().mapToDouble(s -> s.getLocation().getLatitude()).max().orElse(1);
        minLon = valid.stream().mapToDouble(s -> s.getLocation().getLongitude()).min().orElse(0);
        maxLon = valid.stream().mapToDouble(s -> s.getLocation().getLongitude()).max().orElse(1);

        // Prevent division-by-zero if all stops share a coordinate
        if (maxLat == minLat) { maxLat = minLat + 0.001; }
        if (maxLon == minLon) { maxLon = minLon + 0.001; }
    }

    /** Longitude → canvas X pixel (west=left, east=right). */
    public double toPixelX(double longitude) {
        double range   = maxLon - minLon;
        double padding = range * PADDING;
        return ((longitude - minLon + padding) / (range + 2 * padding)) * canvasWidth;
    }

    /**
     * Latitude → canvas Y pixel.
     * Inverted: higher latitude (more north) → smaller Y (top of screen).
     */
    public double toPixelY(double latitude) {
        double range   = maxLat - minLat;
        double padding = range * PADDING;
        return (1.0 - ((latitude - minLat + padding) / (range + 2 * padding))) * canvasHeight;
    }

    /** Linear interpolation between two stops at a given progress (0.0–1.0). */
    public double[] interpolatePixel(Stop fromStop, Stop toStop, double progress) {
        double x1 = toPixelX(fromStop.getLocation().getLongitude());
        double y1 = toPixelY(fromStop.getLocation().getLatitude());
        double x2 = toPixelX(toStop.getLocation().getLongitude());
        double y2 = toPixelY(toStop.getLocation().getLatitude());
        return new double[]{ x1 + (x2 - x1) * progress, y1 + (y2 - y1) * progress };
    }

    public boolean isCalibrated() { return canvasWidth > 0 && canvasHeight > 0; }
}
