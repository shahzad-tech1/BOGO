package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.booking.Booking;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BookingRepository {

    /** Original save — kept for backward compatibility. */
    public int save(int passengerId, int busId, double cost) {
        return saveWithPath(passengerId, busId, cost, null);
    }

    /**
     * Saves a booking and stores the human-readable path description
     * (e.g. "F-10 → G-10 → G-9 → G-8") so it can be shown in the cancel overlay later.
     */
    public int saveWithPath(int passengerId, int busId, double cost, String pathDescription) {
        String sql = "INSERT INTO BOOKING (PassengerID, BusID, Active, Cost, BookingTime, PathDescription)"
                   + " VALUES (?, ?, 1, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, passengerId);
            ps.setInt(2, busId);
            ps.setDouble(3, cost);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, pathDescription);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] saveWithPath failed: " + e.getMessage());
        }
        return -1;
    }

    public boolean cancel(int bookingId, int passengerId) {
        String sql = "UPDATE BOOKING SET Active = 0 WHERE BookingID = ? AND PassengerID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, passengerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookingRepository] cancel failed: " + e.getMessage());
            return false;
        }
    }

    public Booking findById(int bookingId) {
        String sql = "SELECT BookingID, PassengerID, BusID, Active, Cost, BookingTime"
                   + " FROM BOOKING WHERE BookingID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBooking(rs);
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findById failed: " + e.getMessage());
        }
        return null;
    }

    public List<Booking> findAll() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT BookingID, PassengerID, BusID, Active, Cost, BookingTime"
                   + " FROM BOOKING ORDER BY BookingTime DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) bookings.add(mapBooking(rs));
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findAll failed: " + e.getMessage());
        }
        return bookings;
    }

    public List<Booking> findByPassengerId(int passengerId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT BookingID, PassengerID, BusID, Active, Cost, BookingTime"
                   + " FROM BOOKING WHERE PassengerID = ? ORDER BY BookingTime DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bookings.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findByPassengerId failed: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Returns a map of BookingID → PathDescription for the given passenger.
     * Used by the UI to show the route in the cancel overlay.
     */
    public HashMap<Integer, String> findPathDescriptionsByPassengerId(int passengerId) {
        HashMap<Integer, String> map = new HashMap<>();
        String sql = "SELECT BookingID, PathDescription FROM BOOKING WHERE PassengerID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String desc = rs.getString("PathDescription");
                    map.put(rs.getInt("BookingID"), desc != null ? desc : "");
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findPathDescriptionsByPassengerId failed: " + e.getMessage());
        }
        return map;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Timestamp bookingTime = rs.getTimestamp("BookingTime");
        return new Booking(
                rs.getInt("BookingID"),
                rs.getInt("PassengerID"),
                rs.getInt("BusID"),
                rs.getBoolean("Active"),
                rs.getDouble("Cost"),
                bookingTime == null ? null : bookingTime.toLocalDateTime()
        );
    }
}
