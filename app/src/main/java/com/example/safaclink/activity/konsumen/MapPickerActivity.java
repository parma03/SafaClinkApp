package com.example.safaclink.activity.konsumen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safaclink.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapPickerActivity extends AppCompatActivity implements MapEventsReceiver {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapPickerActivity";

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoPoint selectedLocation;
    private Marker selectedLocationMarker;
    private Geocoder geocoder;
    private ExecutorService geocodingExecutor;
    private Handler mainHandler;

    // UI Components
    private TextView tvSelectedAddress;
    private Button btnConfirmLocation;
    private ImageButton btnMyLocation;

    // Default location (Jakarta)
    private final GeoPoint DEFAULT_LOCATION = new GeoPoint(-6.2088, 106.8456);
    private final double DEFAULT_ZOOM = 15.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_map_picker);

        // Initialize views
        initViews();

        // Initialize services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        geocodingExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Setup map
        setupMap();

        // Setup listeners
        setupListeners();

        // Get initial location
        GeoPoint initialLocation = getInitialLocation();
        moveMapToLocation(initialLocation);
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnMyLocation = findViewById(R.id.btnMyLocation);
    }

    private void setupMap() {
        // Configure map view
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set map controller
        IMapController mapController = mapView.getController();
        mapController.setZoom(DEFAULT_ZOOM);

        // Add map events overlay for detecting map movements
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

        // Add scroll listener to detect when map stops moving
        mapView.addOnFirstLayoutListener((v, left, top, right, bottom) -> {
            // Map is ready, update location
            updateSelectedLocation();
        });

        // Add a runnable to detect when map stops scrolling
        final Handler handler = new Handler();
        final Runnable updateLocationRunnable = this::updateSelectedLocation;

        mapView.setOnTouchListener((v, event) -> {
            handler.removeCallbacks(updateLocationRunnable);
            handler.postDelayed(updateLocationRunnable, 500); // Update after 500ms of no movement
            return false; // Let the map handle the touch event
        });
    }

    private void setupListeners() {
        btnConfirmLocation.setOnClickListener(v -> confirmSelectedLocation());
        btnMyLocation.setOnClickListener(v -> moveToCurrentLocation());

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private GeoPoint getInitialLocation() {
        Intent intent = getIntent();
        if (intent.hasExtra("current_lat") && intent.hasExtra("current_lng")) {
            double lat = intent.getDoubleExtra("current_lat", 0.0);
            double lng = intent.getDoubleExtra("current_lng", 0.0);
            if (lat != 0.0 && lng != 0.0) {
                return new GeoPoint(lat, lng);
            }
        }

        // Try to get current location if no initial location provided
        getCurrentLocation();
        return DEFAULT_LOCATION;
    }

    private void updateSelectedLocation() {
        if (mapView != null) {
            // Get center of the map
            selectedLocation = (GeoPoint) mapView.getMapCenter();

            // Update address
            getAddressFromLocation(selectedLocation);

            Log.d(TAG, "Location updated: " + selectedLocation.getLatitude() + ", " + selectedLocation.getLongitude());
        }
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        // Optional: Handle single tap on map
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        // Optional: Handle long press on map
        return false;
    }

    private void getAddressFromLocation(GeoPoint geoPoint) {
        geocodingExecutor.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        geoPoint.getLatitude(), geoPoint.getLongitude(), 1);

                String addressText;
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressText = address.getAddressLine(0);

                    if (addressText == null || addressText.isEmpty()) {
                        // Build address manually if getAddressLine returns null
                        StringBuilder sb = new StringBuilder();
                        if (address.getThoroughfare() != null) {
                            sb.append(address.getThoroughfare()).append(", ");
                        }
                        if (address.getSubLocality() != null) {
                            sb.append(address.getSubLocality()).append(", ");
                        }
                        if (address.getLocality() != null) {
                            sb.append(address.getLocality()).append(", ");
                        }
                        if (address.getAdminArea() != null) {
                            sb.append(address.getAdminArea());
                        }
                        addressText = sb.toString();

                        // Remove trailing comma and space
                        if (addressText.endsWith(", ")) {
                            addressText = addressText.substring(0, addressText.length() - 2);
                        }
                    }

                    if (addressText.isEmpty()) {
                        addressText = "Lat: " + String.format("%.6f", geoPoint.getLatitude()) +
                                ", Lng: " + String.format("%.6f", geoPoint.getLongitude());
                    }
                } else {
                    // Show coordinates if no address found
                    addressText = "Lat: " + String.format("%.6f", geoPoint.getLatitude()) +
                            ", Lng: " + String.format("%.6f", geoPoint.getLongitude());
                }

                // Update UI on main thread
                final String finalAddressText = addressText;
                mainHandler.post(() -> tvSelectedAddress.setText(finalAddressText));

            } catch (IOException e) {
                Log.e(TAG, "Geocoder failed: " + e.getMessage());
                String coordText = "Lat: " + String.format("%.6f", geoPoint.getLatitude()) +
                        ", Lng: " + String.format("%.6f", geoPoint.getLongitude());
                mainHandler.post(() -> tvSelectedAddress.setText(coordText));
            } catch (Exception e) {
                Log.e(TAG, "Error getting address: " + e.getMessage());
                mainHandler.post(() -> tvSelectedAddress.setText("Alamat tidak tersedia"));
            }
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                            moveMapToLocation(currentLocation);
                        } else {
                            // If no last known location, use default
                            moveMapToLocation(DEFAULT_LOCATION);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get current location: " + e.getMessage());
                        Toast.makeText(this, "Gagal mendapatkan lokasi saat ini", Toast.LENGTH_SHORT).show();
                        moveMapToLocation(DEFAULT_LOCATION);
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage());
            moveMapToLocation(DEFAULT_LOCATION);
        }
    }

    private void moveToCurrentLocation() {
        getCurrentLocation();
    }

    private void moveMapToLocation(GeoPoint location) {
        if (mapView != null) {
            IMapController mapController = mapView.getController();
            mapController.animateTo(location);
            mapController.setZoom(DEFAULT_ZOOM);

            // Update selected location after moving
            selectedLocation = location;
            getAddressFromLocation(location);
        }
    }

    private void confirmSelectedLocation() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_lat", selectedLocation.getLatitude());
            resultIntent.putExtra("selected_lng", selectedLocation.getLongitude());

            String address = tvSelectedAddress.getText().toString();
            if (!address.isEmpty() && !address.startsWith("Lat:") && !address.equals("Alamat akan ditampilkan di sini")) {
                resultIntent.putExtra("selected_address", address);
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // Get current center as fallback
            if (mapView != null) {
                selectedLocation = (GeoPoint) mapView.getMapCenter();
                confirmSelectedLocation();
            } else {
                Toast.makeText(this, "Lokasi tidak valid", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk fitur ini", Toast.LENGTH_LONG).show();
                moveMapToLocation(DEFAULT_LOCATION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (geocodingExecutor != null) {
            geocodingExecutor.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}