package com.kimbrough_app.gactivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.kimbrough.gclientlib.G;
import com.kimbrough.gclientlib.GoogleConnectionState;
import com.kimbrough.gclientlib.LocationListenerGClient;
import com.kimbrough.gclientlib.TicksStateUpdate;
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
    private int mServerTimer= 1;
    // handler for threshold time
    private Handler mServerHandler;
    // runnable for threshold time
    private Runnable mServerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        requestPermissions();

        createHandlerAndRunnable();

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
                    double distanceValue = Double.parseDouble(distance);
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
                        double distanceValue = Double.parseDouble(distance);
                        mLocationManager.setThresholdRadius(distanceValue);
                    }

                    String time = mMainBinding.thresholdTimeEditText.getText().toString();
                    if (!time.trim().isEmpty()) {
                        thresholdTime = Integer.parseInt(time);
                        mLocationManager.setThresholdTime(thresholdTime);
                    }else {
                        thresholdTime = mLocationManager.getThresholdTime();
                    }

                    mLocationManager.activateLibrary();
                    startServerTimer();

                } else {
                    requestPermissions();
                }

            }
        });

        mMainBinding.stopMointoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocationManager.deactivateLibrary();
                stopServerTimer();

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

    private void stopServerTimer() {
        mServerHandler.removeCallbacks(mServerRunnable);
    }

    private void startServerTimer() {

        mServerHandler.postDelayed(mServerRunnable, 1_000);
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
    public void onLibraryStateChanged() {
        if (mLocationManager.isLibraryActivated()) {
            mMainBinding.isLibraryActivated.setText(R.string.on_label);
            mServerHandler.post(mServerRunnable);
        } else {
            mMainBinding.isLibraryActivated.setText(R.string.off_label);
        }
    }

    @Override
    public void onMonitoringStateChanged(TicksStateUpdate ticksStateUpdate) {

        switch (ticksStateUpdate) {

            case RED:
                mMainBinding.areWeMointoringForLocationUpdates.setText("R");
                mMainBinding.areWeMointoringForLocationUpdates.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.red));
                break;

            case GREEN:
                mMainBinding.areWeMointoringForLocationUpdates.setText("G");
                mMainBinding.areWeMointoringForLocationUpdates.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.green));
                break;

            case ORANGE:
                mMainBinding.areWeMointoringForLocationUpdates.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.orange));

                mMainBinding.areWeMointoringForLocationUpdates.setText("O");
                break;
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
    public void deliverQuietCircleExpiryParameter(int timerTime, int thresholdTime) {
        mMainBinding.thetaTimeValue.setText(MessageFormat.format("{0}/{1}s", mServerTimer, thresholdTime));
    }

    @Override
    public void deliverQuietCircleRadiusParameters(double distance, double thresholdRadius) {
        mMainBinding.thetaCircValue.setText(MessageFormat.format("{0}/{1}m", distance, thresholdRadius));
    }

    @Override
    public void onchangeInGoogleStateConnection(GoogleConnectionState state) {
        mMainBinding.areLocationUpdatesAvalible.setText(state.toString());

        switch (state) {

            case NEW_UNCONNECTED_SESSION:
                mMainBinding.areLocationUpdatesAvalible.setText("NEW_UNCONNECTED_SESSION");
                mMainBinding.areLocationUpdatesAvalible.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.cyan));
                break;

            case GOOGLE_INIT_CONNECT_FAILURE_TO_ESTABLISH_CONNECTION:
                mMainBinding.areLocationUpdatesAvalible.setText("INIT_CONNECTION_FAILURE");
                mMainBinding.areLocationUpdatesAvalible.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.red));
                break;

            case CONNECTED_TO_GOOGLE_LOCATION_API:
                mMainBinding.areLocationUpdatesAvalible.setText("CONNECTED");
                mMainBinding.areLocationUpdatesAvalible.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.green));

                break;
            case GOOGLE_UPDATE_UNAVAILABLE:
                mMainBinding.areLocationUpdatesAvalible.setText("UPDATE_UNAVAILABLE");
                mMainBinding.areLocationUpdatesAvalible.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.orange));

                break;
            case DISCONNECTED_GOOGLE_API:
                mMainBinding.areLocationUpdatesAvalible.setText("DISCONNECTED");
                mMainBinding.areLocationUpdatesAvalible.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.cyan));

                break;
        }
    }

    @Override
    public void resetServerTimer() {
        mServerTimer = 1;
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

    private void createHandlerAndRunnable() {
        mServerHandler = new Handler();
        mServerRunnable = new Runnable() {
            @Override
            public void run() {

                    mServerTimer++;
                    mServerHandler.postDelayed(mServerRunnable, thresholdTime * 1_000);

            }
        };

    }

}
