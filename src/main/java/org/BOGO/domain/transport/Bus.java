package org.BOGO.domain.transport;
import org.BOGO.domain.user.*;


public class Bus {

    private int busID;
    private String busCompany;
    private String registration;
    private int registrationYear;
    private BusStatus busStatus;

    private int capacity;
    private int currentCapacity;
    private Location location;

    // Health metrics (mapped to new DB columns)
    private double tyreHealth = 100.0;
    private double engineHealth = 100.0;
    private double chassisHealth = 100.0;

    // ---------- Constructors ----------
    public Bus(int ID, int cap) {
        busID = ID;
        capacity = cap;
        currentCapacity = cap;
        busStatus = BusStatus.AVAILABLE;
    }

    public Bus(int busID, String busCompany, String registration, int registrationYear, BusStatus busStatus, int capacity) {
        this.busID = busID;
        this.busCompany = busCompany;
        this.registration = registration;
        this.registrationYear = registrationYear;
        this.busStatus = busStatus;
        this.capacity = capacity;
        this.currentCapacity = capacity;
    }


    // ---------- setters ----------
    public void  setBusID(int ID)          { busID = ID; }
    public void setBusCompany(String busCompany) { this.busCompany = busCompany; }
    public void setRegistration(String registration) { this.registration = registration; }
    public void setRegistrationYear(int registrationYear) { this.registrationYear = registrationYear; }
    public void  setBusStatus(BusStatus bs)      { busStatus = bs; }
    public void  setCapacity(int cap)       { capacity=cap; }
    public void  setCurrentCapacity(int cap) { currentCapacity = cap; }
    public void  setLocation(Location lt)       { location = lt; }
    public void  setTyreHealth(double tyreHealth) { this.tyreHealth = tyreHealth; }
    public void  setEngineHealth(double engineHealth) { this.engineHealth = engineHealth; }
    public void  setChassisHealth(double chassisHealth) { this.chassisHealth = chassisHealth; }

    // ---------- getters ----------
    public int getBusID()          { return busID; }
    public String getBusCompany() { return busCompany; }
    public String getRegistration() { return registration; }
    public int getRegistrationYear() { return registrationYear; }
    public BusStatus getBusStatus()      { return busStatus; }
    public int getCapacity()       { return capacity; }
    public int getCurrentCapacity() { return currentCapacity; }
    public Location getLocation()       { return location; }
    public double getTyreHealth()    { return tyreHealth; }
    public double getEngineHealth()  { return engineHealth; }
    public double getChassisHealth() { return chassisHealth; }


    public boolean allocateDriver(Driver d) {
        boolean returnValue = true;

        return returnValue;
    }

    public boolean removePassenger() {
        boolean returnValue;
        if(currentCapacity>0) {
            currentCapacity--;
            returnValue = true;
        } else {
            returnValue = false;
        }
        return returnValue;
    }

    public boolean addPassenger() {
        boolean returnValue;
        if(currentCapacity<capacity) {
            currentCapacity++;
            returnValue = true;
        } else {
            returnValue = false;
        }
        return returnValue;
    }

    public void setRoute(Route route) {
    }
}
// make function regarding allocation of bus to driver in the driver class
