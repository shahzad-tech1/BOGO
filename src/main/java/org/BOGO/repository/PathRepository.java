package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Stop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PathRepository handles persistence of computed passenger journey paths.
 * Paths and their ordered stops are stored in PATHS and PATH_STOPS tables.
 */
public class PathRepository {

    private final StopRepository stopRepository = new StopRepository();

    /**
     * Saves a computed path and its ordered stop sequence to the database.
     * Sets pathId on the Path object after insertion.
     */
    public int save(Path path) {
        if (path == null || path.getStops() == null || path.getStops().isEmpty()) {
            return -1;
        }

        int originId = path.getOrigin() != null ? path.getOrigin().getStopID()
                : (path.getStops().isEmpty() ? 0 : path.getStops().get(0).getStopID());
        int destId = path.getDestination() != null ? path.getDestination().getStopID()
                : (path.getStops().isEmpty() ? 0 : path.getStops().get(path.getStops().size() - 1).getStopID());

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int pathId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO PATHS (OriginStopId, DestinationStopId, TotalEstimatedTime) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, originId);
                    ps.setInt(2, destId);
                    ps.setInt(3, path.getTotalEstimatedTime());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No generated PathId returned.");
                        pathId = keys.getInt(1);
                    }
                }

                // Insert PATH_STOPS in order
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO PATH_STOPS (PathId, StopId, Position) VALUES (?, ?, ?)")) {
                    List<Stop> stops = path.getStops();
                    for (int i = 0; i < stops.size(); i++) {
                        ps.setInt(1, pathId);
                        ps.setInt(2, stops.get(i).getStopID());
                        ps.setInt(3, i + 1);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                path.setPathId(pathId);
                return pathId;

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[PathRepository] save failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Loads a path and its ordered stops from the database by pathId.
     */
    public Path findById(int pathId) {
        String sql = "SELECT OriginStopId, DestinationStopId, TotalEstimatedTime FROM PATHS WHERE PathId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pathId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Stop origin = stopRepository.findById(rs.getInt("OriginStopId"));
                    Stop dest = stopRepository.findById(rs.getInt("DestinationStopId"));
                    Path path = new Path(pathId, origin, dest);
                    path.setTotalEstimatedTime(rs.getInt("TotalEstimatedTime"));
                    path.setStops(loadPathStops(conn, pathId));
                    return path;
                }
            }
        } catch (SQLException e) {
            System.err.println("[PathRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Deletes a path and its associated PATH_STOPS rows.
     */
    public boolean delete(int pathId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM PATH_STOPS WHERE PathId = ?")) {
                    ps.setInt(1, pathId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM PATHS WHERE PathId = ?")) {
                    ps.setInt(1, pathId);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[PathRepository] delete failed: " + e.getMessage());
            return false;
        }
    }

    private List<Stop> loadPathStops(Connection conn, int pathId) throws SQLException {
        List<Stop> stops = new ArrayList<>();
        String sql = "SELECT StopId FROM PATH_STOPS WHERE PathId = ? ORDER BY Position";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pathId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stop stop = stopRepository.findById(rs.getInt("StopId"));
                    if (stop != null) stops.add(stop);
                }
            }
        }
        return stops;
    }
}
