package com.ka.roadsafetysystem;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil; // Import PolyUtil from the Google Maps Android API utility library

import java.util.List;

public class DirectionsHelper {

    public static List<LatLng> decodePolyline(String encoded) {
        return PolyUtil.decode(encoded);
    }
}