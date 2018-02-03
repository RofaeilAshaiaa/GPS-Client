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

import java.text.MessageFormat;

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
    private int priority = -1;
    private int intervalInSeconds = -1;
    private int thresholdTime = -1;
    private int thresholdDistance = -1;
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
                    if (!updateInterval.trim().isEmpty()) {
                        intervalInSeconds = Integer.parseInt(updateInterval);
                        mLocationManager.setFastestInterval(intervalInSeconds);
                        mLocationManager.setLocationUpdatesFrequency(intervalInSeconds);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "You Should start monitoring first", Toast.LENGTH_LONG).show();
                }
            }
        });

        mMainBinding.changeThresholdDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String distance = mMainBinding.thresholdDistanceEditText.getText().toString();
                if (!distance.trim().isEmpty()) {
                    int distanceValue = Integer.parseInt(distance);
                    mLocationManager.setThresholdRadius(distanceValue);
                }
            }
        });

        mMainBinding.changeThresholdTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = mMainBinding.thresholdTimeEditText.getText().toString();
                if (!time.trim().isEmpty()) {
                    int timeValue = Integer.parseInt(time);
                    mLocationManager.setThresholdTime(timeValue);
                }
            }
        });

        mMainBinding.startMointoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermissions()) {

                    mLocationManager.setPriority(priority);

                    String updateInterval = mMainBinding.locationUpdatesInterval.getText().toString();
                    if (!updateInterval.trim().isEmpty()) {
                        intervalInSeconds = Integer.parseInt(updateInterval);
                        mLocationManager.setFastestInterval(intervalInSeconds);
                        mLocationManager.setLocationUpdatesFrequency(intervalInSeconds);
                    }

                    String distance = mMainBinding.thresholdDistanceEditText.getText().toString();
                    if (!distance.trim().isEmpty()) {
                        int distanceValue = Integer.parseInt(distance);
                        mLocationManager.setThresholdRadius(distanceValue);
                    }

                    String time = mMainBinding.thresholdTimeEditText.getText().toString();
                    if (!time.trim().isEmpty()) {
                        int timeValue = Integer.parseInt(time);
                        mLocationManager.setThresholdTime(timeValue);
                    }

                    mLocationManager.activateLibrary();

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
                        priority = PRIORITY_HIGH_ACCURACY;
                        break;
                    case 1:
                        priority = PRIORITY_BALANCED_POWER_ACCURACY;
                        break;
                    case 2:
                        priority = PRIORITY_LOW_POWER;
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

    }

    @Override
    public void deliverNewLocationUpdate(Location location, String mLastUpdateTime) {

        String value = "<" + location.getLatitude()
                + ", " + location.getLongitude()
                + ", " + mLastUpdateTime
                + ">";

        mMainBinding.lastKnownLocation.setText(value);
    }

    @Override
    public void onLocationAvailabilityChanged(ConnectionState state) {
        if (state == ConnectionState.CONNECTED) {
            mMainBinding.areLocationUpdatesAvalible.setText(R.string.on_label);

        } else if (state == ConnectionState.DISCONNECTED) {
            mMainBinding.areLocationUpdatesAvalible.setText(R.string.off_label);
        }
    }

    @Override
    public void onLibraryStateChanged() {
        if (mLocationManager.isLibraryActivated()) {
            mMainBinding.isLibraryActivated.setText(R.string.on_label);
        } else {
            mMainBinding.isLibraryActivated.setText(R.string.off_label);
        }
    }

    @Override
    public void onMonitoringStateChanged() {
        if (mLocationManager.isClientMonitoringLocation()) {
            mMainBinding.areWeMointoringForLocationUpdates.setText(R.string.on_label);
        } else {
            mMainBinding.areWeMointoringForLocationUpdates.setText(R.string.off_label);
        }
    }

    @Override
    public void deliverSilentTick(Location location, String lastUpdateTime) {

        String value = "<" + location.getLatitude()
                + ", " + location.getLongitude()
                + ", " + lastUpdateTime
                + ">";
        mMainBinding.silentTicks.setText(value);
    }

    @Override
    public void deliverThetaTime(int timerTime, int thresholdTime) {
        mMainBinding.thetaTimeValue.setText(MessageFormat.format("{0}/{1}s", timerTime, thresholdTime));
    }

    @Override
    public void deliverThetaDistance(double distance, double thresholdRadius) {
        mMainBinding.thetaCircValue.setText(MessageFormat.format("{0}/{1}m", distance, thresholdRadius));
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
