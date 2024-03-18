package com.ka.roadsafetysystem;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.ka.roadsafetysystem.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMainBinding binding;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;
    private boolean isFirstLocationUpdate = true;
    private static final String Tag = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
              startLocationUpdates();
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation view item clicks here
                int id = menuItem.getItemId();
                if (id == R.id.admin) {
                    Intent i = new Intent(MainActivity.this, Adminstration.class);
                    startActivity(i);

                }
                // Close the drawer when item is selected
                drawer.closeDrawers();
                return true;
            }
        });
        binding.btnToggleDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the drawer
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getDataFromFirebase();

    }


    private void getDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("fdkjhgfghdjk");
                for (DataSnapshot districtSnapshot : dataSnapshot.getChildren()) {
                    String districtKey = districtSnapshot.getKey();
                    for (DataSnapshot accidentZoneSnapshot : districtSnapshot.getChildren()) {
                        for (DataSnapshot accidentZoneKey : accidentZoneSnapshot.getChildren()) {
                            Double latitude = (Double) accidentZoneKey.child("latitude").getValue();
                            Double longitude = (Double) accidentZoneKey.child("longitude").getValue();

                            String accidentZone = (String) accidentZoneSnapshot.child("accidentZone").getValue();
                            Log.d(Tag, "District: " + districtKey);
                            Log.d(Tag, "Accident Zone Key: " + accidentZoneKey);
                            Log.d(Tag, "Accident Zone: " + accidentZone + ", Latitude: " + latitude + ", Longitude: " + longitude);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    getAddress(latitude, longitude);
                    LatLng latLng = new LatLng(latitude, longitude);
                    System.out.println(latitude + " " + longitude);
                    updateMarkerPosition(latLng);
                    if (isFirstLocationUpdate) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        isFirstLocationUpdate = false;
                    }
                }
            }
        };

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

    private void updateMarkerPosition(LatLng latLng) {
        if (currentLocationMarker != null) {
            currentLocationMarker.setPosition(latLng);
        }
    }

    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                Log.d(TAG, "District: " + address.getSubAdminArea());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng initialLatLng = new LatLng(0, 0);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mylocation);

        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(initialLatLng).title("Current Location").icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialLatLng));


        LatLng origin = new LatLng(-33.867, 151.206);
        LatLng destination = new LatLng(-33.847, 151.195);

        //  drawDirections(origin, destination);
    }

    private void drawDirections(LatLng origin, LatLng destination) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();

        DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING)
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        if (result != null && result.routes.length > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.addPolyline(new PolylineOptions()
                                            .addAll(DirectionsHelper.decodePolyline(result.routes[0].overviewPolyline.getEncodedPath()))
                                            .color(Color.BLUE)
                                            .width(5));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12));
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Failed to get directions", Toast.LENGTH_SHORT).show();
                                Log.e("DirectionsAPI", "Failed to get directions: " + e.getMessage(), e);
                            }
                        });
                    }

                });
    }

}
