package org.BOGO.service;

import org.BOGO.domain.communication.Message;
import org.BOGO.domain.communication.Notification;
import org.BOGO.domain.user.User;
import org.BOGO.repository.NotificationRepository;
import java.util.List;

public class NotificationService {

    private final NotificationRepository notificationRepository = new NotificationRepository();


    /**
     * Creates and delivers a notification to a single user.
     */
    public void sendNotification(User user, Message message) {
        if (user != null && message != null) {
            notificationRepository.save(user.getUserID(), "New message");
        }
    }

    public void sendNotification(int userId, String content) {
        notificationRepository.save(userId, content);
    }

    /**
     * Broadcasts a notification to a list of users simultaneously.
     */
    public void sendBulkNotification(List<User> users, Message message) {
        if (users == null) return;
        for (User user : users) {
            sendNotification(user, message);
        }
    }

    /**
     * Marks the given notification as read.
     */
    public void markNotificationRead(int notificationID) {
        notificationRepository.markRead(notificationID);
    }

    /**
     * Returns all unread notifications for the given user ID.
     */
    public List<Notification> getUnreadNotifications(int userID) {
        return notificationRepository.findUnread(userID);
    }
}
