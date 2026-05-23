package org.BOGO.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import java.util.List;

/**
 * Renders the General Map (admin/driver/passenger dashboards).
 * Shows ALL stops, ALL routes, and ALL buses animated via SimulationState.
 * Called every frame from AnimationTimer — already on FX thread, safe to draw.
 */
public class GeneralMapRenderer {

    private static final Color BG_COLOR      = Color.web("#0a0a1a");
    private static final Color ROUTE_ACTIVE  = Color.web("#00aaff");
    private static final Color ROUTE_INACTIVE= Color.web("#333355");
    private static final Color STOP_COLOR    = Color.web("#dbeafe");
    private static final Color STOP_LABEL    = Color.web("#888888");
    private static final Color BUS_COLOR     = Color.web("#00ff99");
    private static final double ROUTE_WIDTH  = 2.0;
    private static final double STOP_RADIUS  = 5.0;
    private static final double BUS_RADIUS   = 8.0;

    private final Canvas canvas;
    private final CoordinateNormalizer normalizer;

    public GeneralMapRenderer(Canvas canvas, CoordinateNormalizer normalizer) {
        this.canvas     = canvas;
        this.normalizer = normalizer;
    }

    /**
     * Full redraw every frame. No DB calls, no allocations beyond iteration.
     * @param stops      all stops (from MapDataLoader, cached)
     * @param routes     all routes (from MapDataLoader, cached)
     * @param simState   current bus positions (updated by BusSimulationEngine)
     */
    public void draw(List<Stop> stops, List<Route> routes, SimulationState simState) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. Clear
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);
        if (!normalizer.isCalibrated()) return;

        // 2. Route lines
        gc.setLineWidth(ROUTE_WIDTH);
        for (Route route : routes) {
            List<Stop> rs = route.getStops();
            if (rs == null || rs.size() < 2) continue;
            gc.setStroke(route.isActive() ? ROUTE_ACTIVE : ROUTE_INACTIVE);
            for (int i = 0; i < rs.size() - 1; i++) {
                Stop a = rs.get(i); Stop b = rs.get(i + 1);
                if (a.getLocation() == null || b.getLocation() == null) continue;
                gc.strokeLine(
                    normalizer.toPixelX(a.getLocation().getLongitude()),
                    normalizer.toPixelY(a.getLocation().getLatitude()),
                    normalizer.toPixelX(b.getLocation().getLongitude()),
                    normalizer.toPixelY(b.getLocation().getLatitude())
                );
            }
        }

        // 3. Stops
        gc.setFont(Font.font("System", 9));
        for (Stop stop : stops) {
            if (stop.getLocation() == null) continue;
            double x = normalizer.toPixelX(stop.getLocation().getLongitude());
            double y = normalizer.toPixelY(stop.getLocation().getLatitude());
            gc.setFill(stop.isActive() ? STOP_COLOR : Color.web("#444444"));
            gc.fillOval(x - STOP_RADIUS, y - STOP_RADIUS, STOP_RADIUS * 2, STOP_RADIUS * 2);
            gc.setFill(STOP_LABEL);
            gc.fillText(stop.getStopName(), x + STOP_RADIUS + 2, y + 4);
        }

        // 4. Buses
        if (simState != null) {
            for (int tripId : simState.getTripIds()) {
                double[] pos = simState.getPixelPosition(tripId);
                if (pos == null) continue;
                gc.setFill(BUS_COLOR);
                gc.fillOval(pos[0] - BUS_RADIUS, pos[1] - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeOval(pos[0] - BUS_RADIUS, pos[1] - BUS_RADIUS, BUS_RADIUS * 2, BUS_RADIUS * 2);
                gc.setFill(Color.web("#0a0a1a"));
                gc.setFont(Font.font("System", 8));
                gc.fillText("B", pos[0] - 3, pos[1] + 3);
            }
        }
    }
}
