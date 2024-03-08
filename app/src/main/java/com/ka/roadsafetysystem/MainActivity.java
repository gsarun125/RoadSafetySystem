package com.ka.roadsafetysystem;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.ka.roadsafetysystem.databinding.ActivityMainBinding;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ActivityMainBinding binding;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng origin = new LatLng(-33.867, 151.206);
        LatLng destination = new LatLng(-33.847, 151.195);

        // Draw directions on the map
        drawDirections(origin, destination);
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
