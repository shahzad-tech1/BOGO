package org.BOGO.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import java.util.List;

/**
 * Renders the Driver Route Map — shows ONLY the driver's assigned route
 * and ONE bus moving along it.
 * Called every frame from AnimationTimer.
 */
public class DriverRouteMapRenderer {

    private static final Color BG_COLOR     = Color.web("#0a0a1a");
    private static final Color ROUTE_COLOR  = Color.web("#0088ff");
    private static final Color STOP_ACTIVE  = Color.web("#ffffff");
    private static final Color STOP_BLOCKED = Color.web("#ff4444");
    private static final Color BUS_COLOR    = Color.web("#00ff99");
    private static final Color LABEL_COLOR  = Color.web("#aaaaaa");
    private static final double ROUTE_WIDTH = 3.0;
    private static final double STOP_RADIUS = 6.0;
    private static final double BUS_RADIUS  = 9.0;

    private final Canvas canvas;
    private final CoordinateNormalizer normalizer;

    private double busX = 0, busY = 0;

    public DriverRouteMapRenderer(Canvas canvas, CoordinateNormalizer normalizer) {
        this.canvas     = canvas;
        this.normalizer = normalizer;
    }

    public void updateBusPosition(double x, double y) { this.busX = x; this.busY = y; }

    /**
     * Draw the driver's route with one bus moving on it.
     * Called every frame by AnimationTimer — safe on FX thread.
     */
    public void draw(Route driverRoute) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);
        if (!normalizer.isCalibrated() || driverRoute == null) return;

        List<Stop> stops = driverRoute.getStops();
        if (stops == null || stops.size() < 2) return;

        // 1. Route line
        gc.setStroke(ROUTE_COLOR);
        gc.setLineWidth(ROUTE_WIDTH);
        for (int i = 0; i < stops.size() - 1; i++) {
            Stop a = stops.get(i); Stop b = stops.get(i + 1);
            if (a.getLocation() == null || b.getLocation() == null) continue;
            gc.strokeLine(
                normalizer.toPixelX(a.getLocation().getLongitude()),
                normalizer.toPixelY(a.getLocation().getLatitude()),
                normalizer.toPixelX(b.getLocation().getLongitude()),
                normalizer.toPixelY(b.getLocation().getLatitude())
            );
        }

        // 2. Stops
        gc.setFont(Font.font("System", 10));
        for (Stop stop : stops) {
            if (stop.getLocation() == null) continue;
            double x = normalizer.toPixelX(stop.getLocation().getLongitude());
            double y = normalizer.toPixelY(stop.getLocation().getLatitude());
            gc.setFill(stop.isActive() ? STOP_ACTIVE : STOP_BLOCKED);
            gc.fillOval(x - STOP_RADIUS, y - STOP_RADIUS, STOP_RADIUS * 2, STOP_RADIUS * 2);
            gc.setFill(LABEL_COLOR);
            gc.fillText(stop.getStopName(), x + STOP_RADIUS + 2, y + 4);
            if (!stop.isActive()) {
                gc.setFill(STOP_BLOCKED);
                gc.fillText("BLOCKED", x + STOP_RADIUS + 2, y + 14);
            }
        }

        // 3. Bus icon
        if (busX > 0 || busY > 0) {
            gc.setFill(BUS_COLOR);
            gc.fillOval(busX - BUS_RADIUS, busY - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokeOval(busX - BUS_RADIUS, busY - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
        }
    }
}
