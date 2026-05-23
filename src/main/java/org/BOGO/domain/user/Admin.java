package org.BOGO.domain.user;

import org.BOGO.domain.booking.Booking;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.communication.Notification;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import java.util.ArrayList;
import java.util.List;


public class Admin extends User {


    private List<Notification> notifications = new ArrayList<>();

    // ---------- Constructors ----------
    public Admin(int userID, String name, String email, String phoneNumber, String password) {
        super(userID, name, email, phoneNumber, password);
        this.notifications = new ArrayList<>();
    }

    //-------------Business Methods---------------------

    public boolean addDriver(Driver driver, List<Driver> driverList) {
        if (driver == null || driverList.contains(driver)) return false;
        driverList.add(driver);
        return true;
    }
    public boolean removeDriver(Driver driver, List<Driver> driverList) {
        if (driver == null || !driverList.contains(driver)) return false;
        driver.releaseBus();
        driver.goOffDuty();
        driverList.remove(driver);
        return true;
    }
    public boolean addRoute(Route route, List<Route> routeList) {
        if (route == null || routeList.contains(route)) return false;
        routeList.add(route);
        return true;
    }
    public boolean removeRoute(Route route, List<Route> routeList) {
        if (route == null || !routeList.contains(route)) return false;
        routeList.remove(route);
        return true;
    }
    public boolean addStop(Stop stop, List<Stop> stopList) {
        if (stop == null || stopList.contains(stop)) return false;
        stopList.add(stop);
        return true;
    }
    public boolean removeStop(Stop stop, List<Stop> stopList) {
        if (stop == null || !stopList.contains(stop)) return false;
        stopList.remove(stop);
        return true;
    }
    public boolean cancelBooking(Booking booking) {
        if (booking == null) return false;
        return true;
    }
    public List<Booking> viewAllBookings(List<Booking> bookingList) {
        return new ArrayList<>(bookingList);
    }
    public void addNotification(Notification notification) {
        if (notification != null) {
            this.notifications.add(notification);
        }
    }
    public List<Notification> getNotifications() {
        return notifications;
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
    public void sendNotification(Notification notification, List<Notification> recipientList) {
        if (notification != null) {
            recipientList.add(notification);
        }
    }



}
