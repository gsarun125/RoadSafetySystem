package com.ka.roadsafetysystem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ka.roadsafetysystem.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    public static TextToSpeech t1;


    private float GEOFENCE_RADIUS = 20;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";

    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private NavigationView navigationView;
    private final String Tag="MainActivity";
    private DrawerLayout drawer;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);

        if (t1 == null) {
            initializeTextToSpeech();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle navigation view item clicks here
                int id = menuItem.getItemId();
                if (id == R.id.admin) {
                    Intent i = new Intent(MainActivity.this, Adminstration.class);
                    startActivity(i);

                } else if (id==R.id.Adminstratiom) {
                    Intent i = new Intent(MainActivity.this, DisplayDataActivity.class);
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
        getDataFromFirebase();
    }

    private void initializeTextToSpeech() {

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });
    }

    private void getDataFromFirebase() {
        // Enable disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Add a listener for value events
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Loop through all data
                for (DataSnapshot districtSnapshot : dataSnapshot.getChildren()) {
                    String districtKey = districtSnapshot.getKey();
                    for (DataSnapshot accidentZoneSnapshot : districtSnapshot.getChildren()) {
                        String accidentZone = accidentZoneSnapshot.getKey();

                        for (DataSnapshot accidentZoneKey : accidentZoneSnapshot.getChildren()) {

                            Double latitude = accidentZoneKey.child("latitude").getValue(Double.class);
                            Double longitude = accidentZoneKey.child("longitude").getValue(Double.class);


                            // Log the data
                            Log.d(Tag, "District: " + districtKey);
                            Log.d(Tag, "Accident Zone Key: " + accidentZoneKey);
                            Log.d(Tag, "Accident Zone: " + accidentZone + ", Latitude: " + latitude + ", Longitude: " + longitude);

                            LatLng latLng= new LatLng(latitude, longitude);

                            addCircle(latLng, GEOFENCE_RADIUS,accidentZone);
                            String formattedGeofenceId = String.format(Locale.getDefault(), "GEOFENCE_%f_%f_%s", latLng.latitude, latLng.longitude, accidentZone);

                            // Add geofence at the long-pressed location
                            addGeofence(latLng, GEOFENCE_RADIUS,formattedGeofenceId);

                            Log.d(TAG, "Latitude: " + latLng.latitude + ", Longitude: " + latLng.longitude);

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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-33.852, 151.211);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));

        enableUserLocation();

        mMap.setOnMapLongClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                    }
                });

    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                //We do not have the permission..
            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is necessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        } else {
            handleMapLongClick(latLng);
        }

    }

    private void handleMapLongClick(LatLng latLng) {

        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        String accidentZone="TestingZone";
        String formattedGeofenceId = String.format(Locale.getDefault(), "GEOFENCE_%f_%f_%s", latLng.latitude, latLng.longitude, accidentZone);

        // Add geofence at the long-pressed location
        addGeofence(latLng, GEOFENCE_RADIUS,formattedGeofenceId);

        // Log the latitude and longitude
        Log.d(TAG, "Latitude: " + latLng.latitude + ", Longitude: " + latLng.longitude);
    }

    private void addGeofence(LatLng latLng, float radius, String geofenceId) {
        Geofence geofence = geofenceHelper.getGeofence(geofenceId, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }
    private void addCircle(LatLng latLng, float radius  ){
        // Add a marker with a custom icon
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.accident);
        Bitmap resizedBitmap = resizeBitmap(originalBitmap, 72, 72); // Adjust width and height as needed


        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                .position(latLng, 20, 20) // Specify width and height if needed
                .visible(true));

//        mMap.addMarker(new MarkerOptions()
        //              .position(latLng)
        //            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
        //          .anchor(0.5f, 0.5f)); // Set the anchor to the center of the image

        // You can also add a transparent circle around the marker to indicate the radius
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
    private void addCircle(LatLng latLng, float radius ,String accidentZone ){
        // Add a marker with a custom icon
        Bitmap originalBitmap = null;
        if (accidentZone.equals("Accident Zone")) {
            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.accident);
        } else if (accidentZone.equals("School Zone")) {
            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.school);

        } else if (accidentZone.equals("Speed Breaker")) {
            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.speed_bump);

        }else {
            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.road);

        }

        Bitmap resizedBitmap = resizeBitmap(originalBitmap, 72, 72); // Adjust width and height as needed


        mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                .position(latLng, 20, 20) // Specify width and height if needed
                .visible(true));

//        mMap.addMarker(new MarkerOptions()
  //              .position(latLng)
    //            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
      //          .anchor(0.5f, 0.5f)); // Set the anchor to the center of the image

        // You can also add a transparent circle around the marker to indicate the radius
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
    private Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

}
