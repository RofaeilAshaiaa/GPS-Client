package com.kimbrough_app.gactivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.kimbrough.gclientlib.ConnectionState;
import com.kimbrough.gclientlib.G;
import com.kimbrough.gclientlib.LocationListenerGClient;
import com.kimbrough_app.gactivity.databinding.ActivityMainBinding;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER;
import static com.google.android.gms.location.LocationRequest.PRIORITY_NO_POWER;

public class MainActivity extends AppCompatActivity implements LocationListenerGClient {

    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private G mLocationManager;
    private int priority;
    private int intervalInSeconds;
    private ActivityMainBinding mMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        requestPermissions();

        mMainBinding.changeUpdateFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationManager.isLibraryActivated()) {
                    String updateInterval = mMainBinding.locationUpdatesInterval.getText().toString();
                    if (!updateInterval.isEmpty()) {
                        intervalInSeconds = Integer.parseInt(updateInterval);
                        mLocationManager.setFastestInterval(intervalInSeconds);
                        mLocationManager.setLocationUpdatesFrequency(intervalInSeconds);
                    } else {
                        Toast.makeText(MainActivity.this, "insert value first", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "You Should start monitoring first", Toast.LENGTH_LONG).show();
                }
            }
        });

        mMainBinding.startMointoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermissions()) {
                    String updateInterval = mMainBinding.locationUpdatesInterval.getText().toString();
                    if (!updateInterval.isEmpty()) {
                        intervalInSeconds = Integer.parseInt(updateInterval);
                        mLocationManager.setPriority(priority);
                        mLocationManager.setFastestInterval(intervalInSeconds);
                        mLocationManager.setLocationUpdatesFrequency(intervalInSeconds);
                        mLocationManager.activateLibrary();
                    } else {
                        Toast.makeText(MainActivity.this, "insert some values", Toast.LENGTH_LONG).show();
                    }
                } else {
                    requestPermissions();
                }

            }
        });

        mMainBinding.stopMointoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocationManager.deactivateLibrary();
            }
        });

        mMainBinding.prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {


                switch (position) {
                    case 0:
                        priority = PRIORITY_LOW_POWER;
                        break;
                    case 1:
                        priority = PRIORITY_HIGH_ACCURACY;
                        break;
                    case 2:
                        priority = PRIORITY_BALANCED_POWER_ACCURACY;
                        break;
                    case 3:
                        priority = PRIORITY_NO_POWER;
                        break;
                }

                if (mLocationManager.isLibraryActivated()) {
                    mLocationManager.setPriority(priority);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mLocationManager = new G(this);

        mLocationManager.setThresholdRadius(500);

    }

    @Override
    public void deliverNewLocationUpdate(Location location, String mLastUpdateTime) {

        String value = "Broadcasted: lat: " + location.getLatitude()
                + ", long: " + location.getLongitude()
                + ", " + mLastUpdateTime;

        mMainBinding.lastKnownLocation.setText(value);
    }

    @Override
    public void onLocationAvailabilityChanged(ConnectionState state) {
        if (state == ConnectionState.CONNECTED) {
            mMainBinding.areLocationUpdatesAvalible.setText("Is Client Connected: Yes");

        } else if (state == ConnectionState.DISCONNECTED) {
            mMainBinding.areLocationUpdatesAvalible.setText("Is Client Connected: No");
        }
    }

    @Override
    public void onLibraryStateChanged() {
        if (mLocationManager.isLibraryActivated()) {
            mMainBinding.isLibraryActivated.setText("is Library Activated: Yes");
        } else {
            mMainBinding.isLibraryActivated.setText("is Library Activated: No");
        }
    }

    @Override
    public void onMonitoringStateChanged() {
        if (mLocationManager.isClientMonitoringLocation()) {
            mMainBinding.areWeMointoringForLocationUpdates.setText("are We Monitoring For Location Updates: Yes");
        } else {
            mMainBinding.areWeMointoringForLocationUpdates.setText("are We Monitoring For Location Updates: No");
        }
    }

    @Override
    public void deliverSilentTick(Location location, String lastUpdateTime) {

        String value = "Silent Tick: lat: " + location.getLatitude()
                + ", long: " + location.getLongitude()
                + ", " + lastUpdateTime;
        mMainBinding.silentTicks.setText(value);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {

        Log.i(TAG, "Requesting permission");
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

}
