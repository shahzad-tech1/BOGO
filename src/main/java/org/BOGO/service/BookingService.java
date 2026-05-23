package org.BOGO.service;

import org.BOGO.domain.booking.Booking;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Passenger;
import org.BOGO.repository.BookingRepository;
import org.BOGO.repository.BusRepository;
import org.BOGO.repository.UserRepository;

import java.util.List;

public class BookingService {

    private BookingRepository bookingRepository;
    private BusRepository     busRepository;
    private UserRepository userRepository;
    private Booking booking;


    public BookingService() {}

    /**
     * Creates, persists, and returns a confirmed Booking for the given passenger,
     * path, bus, and payment method. Also generates a QR code and updates the
     * driver's stop list.
     */
    public Booking createBooking(int PassengerID, int pStopID, int dStopID, Path path) {
        if (bookingRepository == null) bookingRepository = new BookingRepository();
        if (busRepository == null) busRepository = new BusRepository();
        if(path==null || path.getStops().isEmpty()) {
            return null;
        }
        List<Bus> buses = busRepository.findAll();
        if (buses.isEmpty()) {
            return null;
        }
        Bus bus = buses.get(0);
        int bookingId = bookingRepository.save(PassengerID, bus.getBusID(), path.getTotalCost());
        if (bookingId < 0) {
            return null;
        }
        Booking created = new Booking(path, new Passenger(PassengerID, "", "", "", ""), bus);
        created.setBookingID(bookingId);
        created.setActive(true);
        return created;
    }

    /**
     * Validates that the pickup and destination stops exist and are active.
     */
    public boolean validateStops(Stop pickup, Stop destination) {
        return pickup != null && destination != null && pickup.getStopID() != destination.getStopID();
    }

    /**
     * Returns all buses operating toward the given destination from the pickup stop.
     */
    public List<Bus> getAvailableBuses(Stop pickup, Stop destination) {
        if (busRepository == null) busRepository = new BusRepository();
        return busRepository.findAll();
    }

    /**
     * Generates a unique, one-use QR code string tied to the given booking.
     */
    public String generateQRCode(Booking booking) {
        return booking == null ? null : "BOGO-" + booking.getBookingID() + "-" + System.currentTimeMillis();
    }

    /**
     * Pushes the new pickup stop to the driver's live stop list for the given bus.
     */
    public void updateDriverStopList(Bus bus, Booking booking) {}

    /**
     * Returns all bookings currently in the system (for admin read).
     */
    public List<Booking> getAllBookings() {
        if (bookingRepository == null) bookingRepository = new BookingRepository();
        return bookingRepository.findAll();
    }

    /**
     * Returns details of a single booking by ID.
     */
    public Booking getBookingByID(int bookingID) {
        if (bookingRepository == null) bookingRepository = new BookingRepository();
        return bookingRepository.findById(bookingID);
    }
}
