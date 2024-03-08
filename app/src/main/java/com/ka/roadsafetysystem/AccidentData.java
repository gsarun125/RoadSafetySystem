package com.ka.roadsafetysystem;

public class AccidentData {
    private double latitude;
    private double longitude;
    private String AccidentZone;

    public String getAccidentZone() {
        return AccidentZone;
    }

    public void setAccidentZone(String accidentZone) {
        AccidentZone = accidentZone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
