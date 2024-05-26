package com.ka.roadsafetysystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ka.roadsafetysystem.databinding.ActivityAdminstrationBinding;


public class Adminstration extends FragmentActivity implements OnMapReadyCallback {
    private ActivityAdminstrationBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference mDatabase;
    private AccidentData accidentData;
    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminstrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(this,
                R.array.district_array, android.R.layout.simple_spinner_item);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.districtSpinner.setAdapter(districtAdapter);
        accidentData = new AccidentData();

        ArrayAdapter<CharSequence> accidentZoneAdapter = ArrayAdapter.createFromResource(this,
                R.array.accident_zone_array, android.R.layout.simple_spinner_item);
        accidentZoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.accidentZoneSpinner.setAdapter(accidentZoneAdapter);

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAllHospitalFields()) {
                    submitDataToFirebase();
                    finish();
                } else {
                    Toast.makeText(Adminstration.this, "Please Enter the Information", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean checkAllHospitalFields() {
        String AdminName = binding.ANameedt.getText().toString().trim();
        if (AdminName.isEmpty()) {
            binding.ANameedt.setError("Hospital Description is required");
            binding.ANameedt.requestFocus();
            return false;
        }
        return true;
    }

    private void submitDataToFirebase() {
        String district = binding.districtSpinner.getSelectedItem().toString();
        String accidentZone = binding.accidentZoneSpinner.getSelectedItem().toString();
        accidentData.setAccidentZone(accidentZone);
        String uniqueId = mDatabase.child(district).child(accidentZone).push().getKey();

        mDatabase.child(district).child(accidentZone).child(uniqueId).setValue(accidentData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@NonNull DatabaseError error, @NonNull DatabaseReference ref) {
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Get the user's last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                        MarkerOptions markerOptions = new MarkerOptions().position(currentLocation).title("My Location").draggable(true);
                        double latitude = currentLocation.latitude;
                        double longitude = currentLocation.longitude;
                        accidentData.setLatitude(latitude);
                        accidentData.setLongitude(longitude);
                        mMap.addMarker(markerOptions);
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
