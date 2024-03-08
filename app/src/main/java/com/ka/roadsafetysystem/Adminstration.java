package com.ka.roadsafetysystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ka.roadsafetysystem.databinding.ActivityAdminstrationBinding;

public class Adminstration extends FragmentActivity implements OnMapReadyCallback {
    private ActivityAdminstrationBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private DatabaseReference mDatabase;
    private AccidentData accidentData;
    private static final int MY_LOCATION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminstrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(this,
                R.array.district_array, android.R.layout.simple_spinner_item);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.districtSpinner.setAdapter(districtAdapter);
        accidentData = new AccidentData();
        // Accident Zone Spinner
        ArrayAdapter<CharSequence> accidentZoneAdapter = ArrayAdapter.createFromResource(this,
                R.array.accident_zone_array, android.R.layout.simple_spinner_item);
        accidentZoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.accidentZoneSpinner.setAdapter(accidentZoneAdapter);

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDataToFirebase();
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void submitDataToFirebase() {
        String district = binding.districtSpinner.getSelectedItem().toString();
        String accidentZone = binding.accidentZoneSpinner.getSelectedItem().toString();
        System.out.println("submited");
        accidentData.setAccidentZone(accidentZone);
        String uniqueId = mDatabase.child(district).child(accidentZone).push().getKey();

        mDatabase.child(district).child(accidentZone).child(uniqueId).setValue(accidentData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(Adminstration.this, "Failed to submit data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Adminstration.this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable the My Location layer if the permission has been granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Get and show the user's current location
            getCurrentLocation();

            // Set up a marker click listener
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    // Move the camera to the tapped marker's location
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    return true; // Return true to consume the event and prevent the default behavior
                }
            });

            // Set up a map click listener
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    // Remove the existing marker
                    mMap.clear();

                    // Add a new draggable marker at the tapped location
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker").draggable(true));
                    marker.showInfoWindow(); // Show the info window for the marker

                    // Set up marker drag listener
                    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(Marker marker) {
                            // Called when marker drag starts
                        }

                        @Override
                        public void onMarkerDrag(Marker marker) {
                            // Called while marker is being dragged
                        }

                        @Override
                        public void onMarkerDragEnd(Marker marker) {
                            // Called when marker drag ends
                            LatLng dragPosition = marker.getPosition();
                            double latitude = dragPosition.latitude;
                            double longitude = dragPosition.longitude;

                            // Use latitude and longitude as needed
                            accidentData.setLatitude(latitude);
                            accidentData.setLongitude(longitude);
                        }
                    });
                }
            });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }



    private void getCurrentLocation() {
        // Create a location request
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Create a location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Add a marker at the current location and move the camera
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                }
            }
        };

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove location updates when the activity is destroyed
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}