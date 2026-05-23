package org.BOGO.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import java.util.List;

/**
 * Renders the Booking Map — static (no simulation, no buses).
 * Draws all stops and routes in a dimmed style, then highlights the selected path.
 * Call draw() after initial load and whenever the selected path changes.
 */
public class BookingMapRenderer {

    private static final Color BG_COLOR          = Color.web("#0a0a1a");
    private static final Color ROUTE_DIM         = Color.web("#1a2a3a");
    private static final Color ROUTE_HIGHLIGHTED = Color.web("#00ff99");
    private static final Color STOP_DEFAULT      = Color.web("#aaaaaa");
    private static final Color STOP_ENDPOINT     = Color.web("#ffff00");
    private static final Color STOP_PATH         = Color.web("#00ccff");
    private static final Color LABEL_COLOR       = Color.web("#666666");
    private static final double ROUTE_WIDTH      = 1.5;
    private static final double HIGHLIGHT_WIDTH  = 3.5;
    private static final double STOP_RADIUS      = 4.5;

    private final Canvas canvas;
    private final CoordinateNormalizer normalizer;

    private Path selectedPath = null;
    private Stop originStop   = null;
    private Stop destStop     = null;

    public BookingMapRenderer(Canvas canvas, CoordinateNormalizer normalizer) {
        this.canvas     = canvas;
        this.normalizer = normalizer;
    }

    public void setSelectedPath(Path path, Stop origin, Stop destination) {
        this.selectedPath = path;
        this.originStop   = origin;
        this.destStop     = destination;
    }

    public void clearSelection() {
        this.selectedPath = null;
        this.originStop   = null;
        this.destStop     = null;
    }

    /**
     * Static redraw — no AnimationTimer needed.
     * Call after: (a) initial load, (b) path selection changes, (c) canvas resize.
     */
    public void draw(List<Stop> stops, List<Route> routes) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);
        if (!normalizer.isCalibrated() || stops == null) return;

        // 1. All route lines (dim)
        gc.setStroke(ROUTE_DIM);
        gc.setLineWidth(ROUTE_WIDTH);
        for (Route route : routes) {
            List<Stop> rs = route.getStops();
            if (rs == null || rs.size() < 2) continue;
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

        // 2. Highlighted path on top
        if (selectedPath != null) {
            gc.setStroke(ROUTE_HIGHLIGHTED);
            gc.setLineWidth(HIGHLIGHT_WIDTH);
            List<Stop> ps = selectedPath.getStops();
            if (ps != null && ps.size() >= 2) {
                for (int i = 0; i < ps.size() - 1; i++) {
                    Stop a = ps.get(i); Stop b = ps.get(i + 1);
                    if (a.getLocation() == null || b.getLocation() == null) continue;
                    gc.strokeLine(
                        normalizer.toPixelX(a.getLocation().getLongitude()),
                        normalizer.toPixelY(a.getLocation().getLatitude()),
                        normalizer.toPixelX(b.getLocation().getLongitude()),
                        normalizer.toPixelY(b.getLocation().getLatitude())
                    );
                }
            }
        }

        // 3. All stops
        gc.setFont(Font.font("System", 9));
        for (Stop stop : stops) {
            if (stop.getLocation() == null) continue;
            double x = normalizer.toPixelX(stop.getLocation().getLongitude());
            double y = normalizer.toPixelY(stop.getLocation().getLatitude());

            boolean isEndpoint = (stop.equals(originStop) || stop.equals(destStop));
            boolean isOnPath   = selectedPath != null
                    && selectedPath.getStops() != null
                    && selectedPath.getStops().contains(stop);

            Color stopColor = isEndpoint ? STOP_ENDPOINT : (isOnPath ? STOP_PATH : STOP_DEFAULT);
            gc.setFill(stopColor);
            gc.fillOval(x - STOP_RADIUS, y - STOP_RADIUS, STOP_RADIUS * 2, STOP_RADIUS * 2);
            gc.setFill(LABEL_COLOR);
            gc.fillText(stop.getStopName(), x + STOP_RADIUS + 2, y + 4);
        }
    }
}
