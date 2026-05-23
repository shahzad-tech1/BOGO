package org.BOGO.domain.user;

import org.BOGO.domain.booking.Path;
import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.communication.Notification;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import java.util.ArrayList;
import java.util.List;


public class Driver extends User {


    private Bus allotedBus;

    private String licenseNumber;

    private String status;  // ACTIVE, INACTIVE, ON_DUTY


    private List<Notification> notifications = new ArrayList<>();

    // ---------- Constructors ----------
    public Driver(int userID, String name, String email, String phoneNumber,
                  String password, String licenseNumber) {
        super(userID, name, email, phoneNumber, password);
        this.licenseNumber = licenseNumber;
        this.status = "INACTIVE";
        this.notifications = new ArrayList<>();
    }


    // ---------- Getters ----------
    public Bus              getAllotedBus()     { return allotedBus; }
    public String           getLicenseNumber()  { return licenseNumber; }
    public String           getStatus()        { return status; }
    public List<Notification> getNotifications(){ return notifications; }

    // ---------- Setters ----------
    public void setAllotedBus(Bus allotedBus)               { this.allotedBus    = allotedBus; }
    public void setLicenseNumber(String licenseNumber)      { this.licenseNumber = licenseNumber; }
    public void setStatus(String status)                    { this.status        = status; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    // ---------- Business Methods ----------
    public boolean assignBus(Bus bus) {
        if (bus == null) return false;
        boolean allocated = bus.allocateDriver(this);
        if (allocated) {
            this.allotedBus = bus;
            this.status = "ON_DUTY";
        }
        return allocated;
    }
    public boolean releaseBus() {
        if (this.allotedBus == null) return false;
        this.allotedBus = null;
        this.status = "INACTIVE";
        return true;
    }
    public boolean goOnDuty() {
        if (this.allotedBus == null) return false;
        this.status = "ON_DUTY";
        return true;
    }
    public void goOffDuty() {
        this.status = "INACTIVE";
    }
    public void addNotification(Notification notification) {
        if (notification != null) {
            this.notifications.add(notification);
        }
    }
    public List<Notification> getUnreadNotifications() {
        List<Notification> unread = new ArrayList<>();
        for (Notification n : notifications) {
            if (!n.isRead()) {
                unread.add(n);
            }
        }
        return unread;
    }

}
