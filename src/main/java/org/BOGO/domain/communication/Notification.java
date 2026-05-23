package org.BOGO.domain.communication;

import java.time.LocalDateTime;


public class Notification {


    private int notificationID;

    private String content;

    private boolean isRead;

    private LocalDateTime createdAt;

    private int recipientID;

    // ---------- Constructors ----------
    public Notification() {}
    public Notification(int notificationID, int recipientID, String content, boolean isRead, LocalDateTime createdAt) {
        this.notificationID = notificationID;
        this.recipientID = recipientID;
        this.content = content;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    //----------- Getters -----------------
    public boolean isRead(){ return isRead; }
    public int getNotificationID() { return notificationID; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getRecipientID() { return recipientID; }
    public void setRead(boolean read) { isRead = read; }
}
