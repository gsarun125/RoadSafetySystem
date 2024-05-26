package com.ka.roadsafetysystem;

import static com.ka.roadsafetysystem.MainActivity.t1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_LONG).show();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }

        List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
        Geofence firstGeofence = triggeredGeofences.get(0);
        String[] parts = firstGeofence.getRequestId().split("_");
        double latitude = Double.parseDouble(parts[1]);
        double longitude = Double.parseDouble(parts[2]);
        String Zone = parts[3];
        Log.d(TAG, "Triggered Geofence - Latitude: " + latitude + ", Longitude: " + longitude + Zone);

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                String Prifix1 = "You are entering an ";
                Trilateration(context, latitude, longitude, Zone, Prifix1);
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_LONG).show();
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                String Prifix2 = "You are near the ";
                Trilateration(context, latitude, longitude, Zone, Prifix2);
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_LONG).show();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                String Prifix3 = "";
                //    Trilateration(context, latitude, longitude, Zone, Prifix);
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void Trilateration(Context context, double latitude, double longitude, String zone, String prifix) {
        LocationUtils locationUtils = new LocationUtils(context);
        locationUtils.getCurrentLocation(new LocationUtils.LocationListener() {
            @Override
            public void onLocationReceived(Location location) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
                double distance = calculateDistance(latitude, longitude, currentLatitude, currentLongitude);
                String roundedDistance = String.valueOf(Math.round(distance));
                String Intemation = zone + " about " + roundedDistance + " meters ahead";

                new Thread(() -> TextToSpeech(context, Intemation)).start();

                System.out.println("distance" + roundedDistance);
            }
        });

    }

    private void TextToSpeech(Context context, String intemation) {
        float speechRate = 0.5f;
        t1.setSpeechRate(speechRate);
        t1.speak(intemation, TextToSpeech.QUEUE_ADD, null);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // Distance in meters
        return distance;
    }
}
