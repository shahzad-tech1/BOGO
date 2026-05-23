package org.BOGO.repository;

import org.BOGO.config.DatabaseConfig;
import org.BOGO.domain.communication.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    public int save(int recipientUserId, String content) {
        String sql = "INSERT INTO NOTIFICATIONS (RecipientUserId, Content, IsRead, CreatedAt) VALUES (?, ?, 0, GETDATE())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, recipientUserId);
            ps.setString(2, content);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationRepository] save failed: " + e.getMessage());
        }
        return -1;
    }

    public List<Notification> findUnread(int recipientUserId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT NotificationId, RecipientUserId, Content, IsRead, CreatedAt FROM NOTIFICATIONS WHERE RecipientUserId = ? AND IsRead = 0";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipientUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CreatedAt");
                    notifications.add(new Notification(
                            rs.getInt("NotificationId"),
                            rs.getInt("RecipientUserId"),
                            rs.getString("Content"),
                            rs.getBoolean("IsRead"),
                            createdAt == null ? null : createdAt.toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationRepository] findUnread failed: " + e.getMessage());
        }
        return notifications;
    }

    public boolean markRead(int notificationId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE NOTIFICATIONS SET IsRead = 1 WHERE NotificationId = ?")) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationRepository] markRead failed: " + e.getMessage());
            return false;
        }
    }
}
