package org.BOGO.domain.booking;

import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.user.Passenger;
import java.time.LocalDateTime;

public class Booking {

    private int bookingID;
    private boolean active;

    private Path path;
    private int pathID;

    private Passenger passenger;
    private int passengerID;

    private Bus bus;
    private int busID;

    private double cost;

    private LocalDateTime createdAt;

    public Booking(Path p, Passenger passenger, Bus bus) {
        this.createdAt = LocalDateTime.now();
        this.path = p;
        this.passenger = passenger;
        this.bus = bus;
        this.cost = path.getTotalCost();
    }

    public Booking(int pid, int Pid, int bid, double cost) {
        this.createdAt = LocalDateTime.now();
        this.pathID = pid;
        this.passengerID = Pid;
        this.busID = bid;
        this.cost = cost;
    }

    public Booking(int bookingID, int passengerID, int busID, boolean active, double cost, LocalDateTime createdAt) {
        this.bookingID = bookingID;
        this.passengerID = passengerID;
        this.busID = busID;
        this.active = active;
        this.cost = cost;
        this.createdAt = createdAt;
    }

    public Booking(Path p, Passenger passenger, Bus bus, LocalDateTime createdAt) {
        this.path = p;
        this.passenger = passenger;
        this.bus = bus;
        this.cost = path.getTotalCost();
        this.createdAt = createdAt;
    }

    // ---------- Business Methods ----------
    /** Marks this booking as COMPLETED and deactivates it. */
    public void confirm() { this.active = true; }

    /** Marks this booking as CANCELLED and deactivates it. */
    public void cancel() { this.active = false; }


    // Getters
    public int             getBookingID()     { return bookingID; }
    public boolean         isActive()         { return active; }
    public Path            getPath()          { return path; }
    public Passenger       getPassenger()     { return passenger; }
    public Bus             getBus()           { return bus; }
    public int             getPassengerID()   { return passenger != null ? passenger.getUserID() : passengerID; }
    public int             getBusID()         { return bus != null ? bus.getBusID() : busID; }
    public double          getCost()          { return cost; }
    public LocalDateTime   getCreatedAt()     { return createdAt; }

    // Setters
    public void setBookingID(int bookingID)           { this.bookingID     = bookingID; }
    public void setActive(boolean active)             { this.active        = active; }
    public void setPath(Path path)                    { this.path          = path; }
    public void setPassenger(Passenger passenger)     { this.passenger     = passenger; }
    public void setBus(Bus bus)                       { this.bus           = bus; }
    public void setCost(double cost)                  { this.cost          = cost; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt     = createdAt; }
}
