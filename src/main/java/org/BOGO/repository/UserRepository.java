package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.user.Admin;
import org.BOGO.domain.user.Driver;
import org.BOGO.domain.user.Passenger;
import org.BOGO.domain.user.User;

import java.sql.*;

public class UserRepository {
    public User findUserByEmail(String email) {
        String sql = """
                SELECT u.UserId, pd.PdId, pd.Name, pd.Email, pd.Password, pd.CNIC,
                       d.DriverID,
                       CASE
                         WHEN a.UserId IS NOT NULL THEN 'ADMIN'
                         WHEN d.UserId IS NOT NULL THEN 'DRIVER'
                         WHEN p.UserId IS NOT NULL THEN 'PASSENGER'
                       END AS RoleName
                FROM PERSONAL_DETAILS pd
                JOIN USERS u ON u.PdId = pd.PdId
                LEFT JOIN ADMIN a ON a.UserId = u.UserId
                LEFT JOIN DRIVER d ON d.UserId = u.UserId
                LEFT JOIN PASSENGER p ON p.UserId = u.UserId
                WHERE pd.Email = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] findUserByEmail failed: " + e.getMessage());
        }
        return null;
    }

    public User findUserById(int userId) {
        String sql = """
                SELECT u.UserId, pd.PdId, pd.Name, pd.Email, pd.Password, pd.CNIC,
                       d.DriverID,
                       CASE
                         WHEN a.UserId IS NOT NULL THEN 'ADMIN'
                         WHEN d.UserId IS NOT NULL THEN 'DRIVER'
                         WHEN p.UserId IS NOT NULL THEN 'PASSENGER'
                       END AS RoleName
                FROM USERS u
                JOIN PERSONAL_DETAILS pd ON pd.PdId = u.PdId
                LEFT JOIN ADMIN a ON a.UserId = u.UserId
                LEFT JOIN DRIVER d ON d.UserId = u.UserId
                LEFT JOIN PASSENGER p ON p.UserId = u.UserId
                WHERE u.UserId = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] findUserById failed: " + e.getMessage());
        }
        return null;
    }

    /** Looks up a driver by their alphanumeric DriverID (e.g. "DR-001"), used for driver login. */
    public User findUserByDriverId(String driverId) {
        String sql = """
                SELECT u.UserId, pd.Name, pd.Email, pd.Password, pd.CNIC,
                       d.DriverID, 'DRIVER' AS RoleName
                FROM DRIVER d
                JOIN USERS u         ON u.UserId  = d.UserId
                JOIN PERSONAL_DETAILS pd ON pd.PdId = u.PdId
                WHERE d.DriverID = ?
                """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] findUserByDriverId failed: " + e.getMessage());
        }
        return null;
    }

    public PersonalDetails findByEmail(String email) {
        String sql = "SELECT PdId, Name, Email, Password, CNIC FROM PERSONAL_DETAILS WHERE Email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPersonalDetails(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserRepository] findByEmail failed: " + e.getMessage());
        }
        return null;
    }

    public PersonalDetails findById(int userID) {
        User user = findUserById(userID);
        return user == null ? null : user.getPersonalDetails();
    }

    public String getUserType(int userID) {
        User user = findUserById(userID);
        return user == null ? null : user.getClass().getSimpleName().toUpperCase();
    }

    public int save(PersonalDetails details, String role) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return save(details, role, null, conn);
        } catch (SQLException e) {
            System.err.println("[UserRepository] save failed: " + e.getMessage());
            return -1;
        }
    }

    public int save(PersonalDetails details, String role, String driverId, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int pdId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO PERSONAL_DETAILS (Name, Email, Password, CNIC) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, details.getName());
                ps.setString(2, details.getEmail());
                ps.setString(3, details.getPassword());
                ps.setString(4, details.getCNIC());
                ps.executeUpdate();
                pdId = readGeneratedInt(ps);
            }

            int userId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO USERS (PdId) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pdId);
                ps.executeUpdate();
                userId = readGeneratedInt(ps);
            }

            String normalizedRole = role == null ? "PASSENGER" : role.trim().toUpperCase();
            switch (normalizedRole) {
                case "ADMIN" -> insertRole(conn, "ADMIN", userId, null);
                case "DRIVER" -> insertRole(conn, "DRIVER", userId, driverId);
                default -> insertRole(conn, "PASSENGER", userId, null);
            }
            conn.commit();
            details.setPdId(pdId);
            return userId;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE PERSONAL_DETAILS SET Password = ? WHERE Email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserRepository] updatePassword failed: " + e.getMessage());
            return false;
        }
    }

    public void saveSessionToken(int userID, String token) {
        // The supplied schema has no session table. Keep this as a no-op so auth stays schema-safe.
    }

    public void deleteSessionToken(int userID) {
    }

    public boolean sessionTokenExists(String token) {
        return token != null && !token.isBlank();
    }

    private void insertRole(Connection conn, String table, int userId, String driverId) throws SQLException {
        String sql = "DRIVER".equals(table)
                ? "INSERT INTO DRIVER (UserId, DriverID) VALUES (?, ?)"
                : "INSERT INTO " + table + " (UserId) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if ("DRIVER".equals(table)) {
                ps.setString(2, driverId);
            }
            ps.executeUpdate();
        }
    }

    private int readGeneratedInt(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("No generated key returned.");
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("UserId");
        String name = rs.getString("Name");
        String email = rs.getString("Email");
        String password = rs.getString("Password");
        String cnic = rs.getString("CNIC");
        String role = rs.getString("RoleName");
        if ("ADMIN".equals(role)) {
            return new Admin(userId, name, email, cnic, password);
        }
        if ("DRIVER".equals(role)) {
            return new Driver(userId, name, email, cnic, password, rs.getString("DriverID"));
        }
        return new Passenger(userId, name, email, cnic, password);
    }

    private PersonalDetails mapPersonalDetails(ResultSet rs) throws SQLException {
        return new PersonalDetails(
                rs.getInt("PdId"),
                rs.getString("Name"),
                rs.getString("Email"),
                rs.getString("CNIC"),
                rs.getString("Password")
        );
    }
}
