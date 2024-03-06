package com.bfmapps.locationsender;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    EditText endpointInput;
    TextView locationText;
    Button sendButton;
    Button displayLocationButton;

    private LocationCallback locationCallback;


    private FusedLocationProviderClient fusedLocationClient;

    private LocationManager locationManager;

    private boolean permissionsGranted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);


        endpointInput = findViewById(R.id.endpoint_edit_text);
        locationText = findViewById(R.id.location_text);
        sendButton = findViewById(R.id.send_button);
        displayLocationButton = findViewById(R.id.display_location_button);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("MainActivity", "location is null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    Log.d("MainActivity", location.toString());
                }
            }
        };



        displayLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });


    }

    void askForPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET) ==
                PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = true;
            Log.d("MainActivity", "permissionsGranted: " + permissionsGranted);

        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 1
            );
        }
    }


    @SuppressLint("MissingPermission")
    void startLocationUpdates() {
        if (permissionsGranted) {
            LocationRequest request = new LocationRequest.Builder(3000)
                    .setIntervalMillis(3000)
                    .setMinUpdateIntervalMillis(1000)
                    .setGranularity(Granularity.GRANULARITY_FINE)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build();

            fusedLocationClient.requestLocationUpdates(request,
                    locationCallback,
                    Looper.getMainLooper());
        } else {
            askForPermissions();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    void getCurrentLocation() {
        if (permissionsGranted) {

            CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder()
                    .setGranularity(Granularity.GRANULARITY_FINE)
                    .setDurationMillis(5000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(0)
                    .build();

            CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();


            fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.getToken()).addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.d("MainActivity", "getCurrentLocation successful");

                    if (location != null) {
                        locationText.setText("Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
                        locationText.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("MainActivity", "location is null");
                        Log.d("MainActivity", "locationAvailability:" + fusedLocationClient.getLocationAvailability());
                    }
                }


            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.d("MainActivity", "getCurrentLocation cancelled");
                }
            });
        } else {
            askForPermissions();
        }

    }



    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();

    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }





}
