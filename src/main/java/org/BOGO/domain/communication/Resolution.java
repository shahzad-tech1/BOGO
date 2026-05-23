package org.BOGO.domain.communication;

import java.time.LocalDateTime;

public class Resolution {
    private int resolutionId;
    private int alertId;
    private int adminId;
    private Integer newBusId;
    private Integer newDriverId;
    private Integer newRouteId;
    private LocalDateTime resolvedAt;

    public int getResolutionId() { return resolutionId; }
    public int getAlertId() { return alertId; }
    public int getAdminId() { return adminId; }
    public Integer getNewBusId() { return newBusId; }
    public Integer getNewDriverId() { return newDriverId; }
    public Integer getNewRouteId() { return newRouteId; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolutionId(int resolutionId) { this.resolutionId = resolutionId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public void setNewBusId(Integer newBusId) { this.newBusId = newBusId; }
    public void setNewDriverId(Integer newDriverId) { this.newDriverId = newDriverId; }
    public void setNewRouteId(Integer newRouteId) { this.newRouteId = newRouteId; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
