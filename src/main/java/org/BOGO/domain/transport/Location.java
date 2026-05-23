package org.BOGO.domain.transport;


public class Location {

    private double latitude;
    private double longitude;

    // ---------- Constructors ----------
    public Location() {}
    public Location(double latitude, double longitude) {
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    //----------Displayer-----------
    public void displayLocation() {
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
    }


}
