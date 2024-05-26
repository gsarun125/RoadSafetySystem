package com.ka.roadsafetysystem;

import android.provider.DocumentsContract;

public class AccidentData {
    private double latitude;
    private double longitude;
    private String AccidentZone;
    private String district;

    private String RootId;

    public String getRootId() {
        return RootId;
    }

    public void setRootId(String rootId) {
        RootId = rootId;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
    public AccidentData(){

    }

    public AccidentData(String district, String accidentZone, double latitude, double longitude,String RootId) {
        this.district = district;
        this.AccidentZone = accidentZone;
        this.latitude = latitude;
        this.longitude = longitude;
       this.RootId= RootId;
    }

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
