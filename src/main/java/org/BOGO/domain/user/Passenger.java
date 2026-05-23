package org.BOGO.domain.user;

import org.BOGO.domain.booking.Booking;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.transport.Bus;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;


public class Passenger extends User {


    private List<Booking> bookings = new ArrayList<>();

    // ---------- Constructors ----------
    public Passenger(int userID, String name, String email, String phoneNumber, String password) {
        super(userID, name, email, phoneNumber, password);
        this.bookings = new ArrayList<>();
    }


    // ---------- Getters ----------
    public List<Booking>  getBookings() { return bookings; }

    // ---------- Setters ----------
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    // ---------- Business Methods ----------
    public Booking createBooking(Path path, Bus bus, String paymentMethod) {
        if (path == null || bus == null) return null;

        boolean added = bus.addPassenger();
        if (!added) return null;  // bus is full

//        Booking booking = new Booking();
//        booking.setPassenger(this);
//        booking.setPath(path);
//        booking.setBus(bus);
//        booking.setCreatedAt(LocalDateTime.now());
//        booking.setActive(true);

//        this.bookings.add(booking);
        return null;
    }
    public boolean cancelBooking(Booking booking) {
        if (booking == null || !bookings.contains(booking)) return false;
        booking.cancel();
        booking.getBus().removePassenger();
        return true;
    }
    public List<Booking> getActiveBookings() {
        List<Booking> active = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.isActive()) {
                active.add(b);
            }
        }
        return active;
    }
    public List<Booking> getBookingHistory() {
//        List<Booking> history = new ArrayList<>();
//        for (Booking b : bookings) {
//            if ("COMPLETED".equals(b.getStatus()) || "CANCELLED".equals(b.getStatus())) {
//                history.add(b);
//            }
//        }
        return null;
    }
    public boolean hasActiveBooking() {
        for (Booking b : bookings) {
            if (b.isActive()) return true;
        }
        return false;
    }


}
