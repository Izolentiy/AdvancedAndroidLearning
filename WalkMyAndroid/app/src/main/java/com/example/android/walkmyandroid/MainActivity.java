/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.walkmyandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted {

    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final String TAG = "MainActivityTAG";
    private static final String TRACKING_LOCATION_KEY = "tracking_location";

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private LocationCallback mLocationCallback;
    private boolean mTrackingLocation = false;

    // View variables
    private TextView mLocationTextView;
    private Button mLocationButton;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationTextView = findViewById(R.id.textview_location);
        mLocationButton = findViewById(R.id.button_location);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTrackingLocation) {
                    stopTrackingLocation();
                } else {
                    startTrackingLocation();
                }
            }
        });

        mAndroidImageView = findViewById(R.id.imageview_android);
        mRotateAnim = (AnimatorSet) AnimatorInflater
                .loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // If tracking is turned on, reverse geocode into address
                if (mTrackingLocation) {
                    new FetchAddressTask(MainActivity.this,
                            MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY , mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        if (mTrackingLocation)
            startTrackingLocation();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mTrackingLocation)
            stopTrackingLocation();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startTrackingLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.requestLocationUpdates(
                    getLocationRequest(), mLocationCallback, null);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Start the reverse geocode AsyncTask
                        new FetchAddressTask(MainActivity.this,
                                MainActivity.this).execute(location);

//                        // Get the location in longitude and latitude
//                        mLastLocation = location;
//                        mLocationTextView.setText(
//                                getString(R.string.location_text,
//                                        mLastLocation.getLatitude(),
//                                        mLastLocation.getLongitude(),
//                                        mLastLocation.getTime()));
                    } else {
                        mLocationTextView.setText(R.string.no_location);
                    }
                }
            });
        }

        // Set up the loading text
        mLocationTextView.setText(getString(
                R.string.address_text, "Loading",
                System.currentTimeMillis()));

        // Start the animation
        mRotateAnim.start();
        mTrackingLocation = true;
        mLocationButton.setText(R.string.stop_tracking_location);
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mLocationButton.setText(R.string.start_tracking_location);
            mLocationTextView.setText(R.string.textview_hint);
            mRotateAnim.end();
            mAndroidImageView.setRotation(0);
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onTaskCompleted(String result) {
        if (mTrackingLocation) {
            // Update the UI
            mLocationTextView.setText(getString(
                    R.string.address_text, result, System.currentTimeMillis()));
        }
    }
}
