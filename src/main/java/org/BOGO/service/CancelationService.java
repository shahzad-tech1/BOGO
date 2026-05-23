package org.BOGO.service;

import org.BOGO.domain.booking.Booking;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.user.Passenger;
import org.BOGO.repository.BookingRepository;

public class CancelationService {

    private final BookingRepository bookingRepository = new BookingRepository();

    public CancelationService() {}

    /**
     * Cancels the specified booking for the passenger.
     * Triggers refund if e-wallet was used. Returns true on success.
     */
    public boolean cancelBooking(int bookingID, int passengerID) {
        return bookingRepository.cancel(bookingID, passengerID);
    }

    /**
     * Checks whether the booking is still eligible for cancellation
     * (bus has not yet arrived at pickup stop).
     */
    public boolean checkEligibility(Booking booking) {
        return booking != null && booking.isActive();
    }

    /**
     * Issues a refund to the passenger's e-wallet and records the transaction.
     */

    /**
     * Removes the cancelled passenger's stop from the driver's live stop list
     * if no other passengers require it.
     */
    public void updateDriverStopList(Bus bus, Booking booking) {}
}
