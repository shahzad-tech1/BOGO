package org.BOGO.domain.communication;

import java.time.LocalDateTime;

public class Alert {
    private int alertId;
    private int senderDriverId;
    private String alertType;
    private String priority;
    private String message;
    private String status;
    private LocalDateTime sentTime;

    public Alert() {
    }

    public Alert(int alertId, int senderDriverId, String alertType, String priority, String message, String status, LocalDateTime sentTime) {
        this.alertId = alertId;
        this.senderDriverId = senderDriverId;
        this.alertType = alertType;
        this.priority = priority;
        this.message = message;
        this.status = status;
        this.sentTime = sentTime;
    }

    public int getAlertId() { return alertId; }
    public int getSenderDriverId() { return senderDriverId; }
    public String getAlertType() { return alertType; }
    public String getPriority() { return priority; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public LocalDateTime getSentTime() { return sentTime; }
    public void setAlertId(int alertId) { this.alertId = alertId; }
    public void setSenderDriverId(int senderDriverId) { this.senderDriverId = senderDriverId; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(String status) { this.status = status; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }
}
