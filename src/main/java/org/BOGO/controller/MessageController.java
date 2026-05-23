package org.BOGO.controller;

import org.BOGO.service.HandleDriverService;
import org.BOGO.service.IssueReportingService;
import org.BOGO.service.IssueResolvingService;
import org.BOGO.service.NotificationService;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Location;
import org.BOGO.domain.user.Driver;

public class MessageController {

    private final IssueReportingService  issueReportingService = new IssueReportingService();
    private final IssueResolvingService  issueResolvingService = new IssueResolvingService();
    private final NotificationService    notificationService = new NotificationService();

    public String reportIssue(Driver driver, String type, String details, Location location) {
        return issueReportingService.reportIssue(driver, type, details, location);
    }

    public void flagBus(Bus bus) {
        issueReportingService.flagBus(bus);
    }

    public boolean resolveBusDown(int alertId, int adminId, int oldBusId, int driverUserId) {
        return issueResolvingService.resolveBusDown(alertId, adminId, oldBusId, driverUserId);
    }

    public void notifyUser(int userId, String content) {
        notificationService.sendNotification(userId, content);
    }
}
