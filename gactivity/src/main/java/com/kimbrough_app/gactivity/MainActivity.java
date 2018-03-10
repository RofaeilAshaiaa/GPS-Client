package com.kimbrough_app.gactivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.kimbrough.gclientlib.LocationListenerGClient;
import com.kimbrough.gclientlib.LocationTickUpdatesReceiver;
import com.kimbrough.gclientlib.LocationUpdatesForegroundService;
import com.kimbrough.gclientlib.helper.GoogleConnectionState;
import com.kimbrough.gclientlib.helper.TicksStateUpdate;
import com.kimbrough.gclientlib.utils.Utils;
import com.kimbrough_app.gactivity.databinding.ActivityMainBinding;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER;
import static com.google.android.gms.location.LocationRequest.PRIORITY_NO_POWER;
import static com.kimbrough.gclientlib.utils.Utils.EXTRA_DATE;
import static com.kimbrough.gclientlib.utils.Utils.EXTRA_LOCATION;
import static com.kimbrough.gclientlib.utils.Utils.EXTRA_TYPE_OF_TICK;

public class MainActivity extends AppCompatActivity implements LocationListenerGClient {

    public static final int UPDATE_UI_DELAY_MILLIS = 250;
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private int priority = -1;
    private int intervalInSeconds = -1;
    private int thresholdTime = -1;
    private int thresholdDistance = -1;
    private ActivityMainBinding mMainBinding;
    // handler for threshold time
    private Handler mCountdownTimerHandler;
    // runnable for threshold time
    private Runnable mCountdownTimerRunnable;

    private boolean mLibraryState;

    // runnable for Service Binder
    private Runnable mServiceBinderTimerRunnable;
    // handler for Service Binder
    private Handler mServiceBinderTimerHandler;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private LocationTickUpdatesReceiver locationTickUpdatesReceiver;

    private boolean mDefaultSelectionOfSpinnerCalled = false;

    // A reference to the service used to get location updates.
    private LocationUpdatesForegroundService mServiceLocationManager = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Tracks the bound state of the service.
    private boolean mStartMonitoringClicked = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesForegroundService.LocalBinder binder =
                    (LocationUpdatesForegroundService.LocalBinder) service;
            mServiceLocationManager = binder.getService();
            mBound = true;
            if (mStartMonitoringClicked) {
                startMonitoringClicked();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceLocationManager = null;
            mBound = false;
        }
    };
    /**
     * Receiver for broadcasts sent by the service.
     */
    private LocationTickUpdatesReceiver mTickUpdatesReceiver = new LocationTickUpdatesReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String locationTickType = intent.getStringExtra(EXTRA_TYPE_OF_TICK);
            Date date = new Date(intent.getLongExtra(EXTRA_DATE, 0));
            Location location = intent.getParcelableExtra(EXTRA_LOCATION);

            switch (locationTickType) {
                case Utils.LocationTickType.BROADCAST_TICK:
                    setBroadcastTickToUI(location, date);
                    break;

                case Utils.LocationTickType.INTERNAL_TICK:
                    setInternalTickToUI(location, date);
                    break;

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTickUpdatesReceiver = new LocationTickUpdatesReceiver();

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        requestPermissions();

        createHandlerAndRunnable();

        mMainBinding.changeUpdateFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mServiceLocationManager.isLibraryActivated()) {
                    String updateInterval = mMainBinding.locationUpdatesInterval.getText().toString();
                    if (!updateInterval.trim().isEmpty()) {
                        intervalInSeconds = Integer.parseInt(updateInterval);
                        mServiceLocationManager.setFastestInterval(intervalInSeconds);
                        mServiceLocationManager.setLocationUpdatesFrequency(intervalInSeconds);
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
                    mServiceLocationManager.setThresholdRadius(distanceValue);
                }
            }
        });

        mMainBinding.changeThresholdTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = mMainBinding.thresholdTimeEditText.getText().toString();
                if (!time.trim().isEmpty()) {
                    thresholdTime = Integer.parseInt(time);
                    mServiceLocationManager.setThresholdTime(thresholdTime);
                }
            }
        });

        mMainBinding.startMointoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermissions()) {

                    Utils.initializeLibrary(MainActivity.this, mServiceConnection);

                    if (mBound) {
                        startMonitoringClicked();
                    } else {
                        mStartMonitoringClicked = true;
                    }

                } else {
                    requestPermissions();
                }

            }
        });

        mMainBinding.stopMointoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mServiceLocationManager.deactivateLibrary();
                unbindService(mServiceConnection);
                stopServiceBinderTimer();
                stopCountdownTimer();
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

                if (mDefaultSelectionOfSpinnerCalled && mServiceLocationManager.isLibraryActivated()) {
                    mServiceLocationManager.setPriority(priority);
                } else {
                    mDefaultSelectionOfSpinnerCalled = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void startMonitoringClicked() {
        mServiceLocationManager.activateLibrary(MainActivity.this);

        mServiceLocationManager.setPriority(priority);

        String updateInterval = mMainBinding.locationUpdatesInterval.getText().toString();
        if (!updateInterval.trim().isEmpty()) {
            intervalInSeconds = Integer.parseInt(updateInterval);
            mServiceLocationManager.setFastestInterval(intervalInSeconds);
            mServiceLocationManager.setLocationUpdatesFrequency(intervalInSeconds);
        }

        String distance = mMainBinding.thresholdDistanceEditText.getText().toString();
        if (!distance.trim().isEmpty()) {
            double distanceValue = Double.parseDouble(distance);
            mServiceLocationManager.setThresholdRadius(distanceValue);
        }

        String time = mMainBinding.thresholdTimeEditText.getText().toString();
        if (!time.trim().isEmpty()) {
            thresholdTime = Integer.parseInt(time);
            mServiceLocationManager.setThresholdTime(thresholdTime);
        } else {
            thresholdTime = mServiceLocationManager.getThresholdTime();
        }

        startServiceBinderTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationTickUpdatesReceiver,
                new IntentFilter(Utils.ACTION_BROADCAST_LOCATION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationTickUpdatesReceiver);
        super.onPause();
    }

    private void stopCountdownTimer() {
        mCountdownTimerHandler.removeCallbacks(mCountdownTimerRunnable);
    }

    private void startCountdownTimer() {
        mCountdownTimerHandler.post(mCountdownTimerRunnable);
    }

    private void stopServiceBinderTimer() {
        mServiceBinderTimerHandler.removeCallbacks(mServiceBinderTimerRunnable);
    }

    private void startServiceBinderTimer() {
        mServiceBinderTimerHandler.post(mServiceBinderTimerRunnable);
    }

    @Override
    public void deliverBroadcastLocationUpdate(Location location, Date lastUpdateTime) {
        setBroadcastTickToUI(location, lastUpdateTime);
    }

    private void setBroadcastTickToUI(Location location, Date lastUpdateTime) {
        String formattedDate = DateFormat.getTimeInstance().format(lastUpdateTime);
        String value = "<" + location.getLatitude()
                + ", " + location.getLongitude()
                + ", " + formattedDate
                + ">";
        mMainBinding.lastKnownLocation.setText(value);
    }

    @Override
    public void onLibraryStateChanged() {

        changeLibraryState();
    }

    private void changeLibraryState() {
        boolean state = mServiceLocationManager.isLibraryActivated();

        if (state) {
            mMainBinding.isLibraryActivated.setText(R.string.on_label);
            mMainBinding.isLibraryActivated.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.green));
        } else {
            mMainBinding.isLibraryActivated.setText(R.string.off_label);
            mMainBinding.isLibraryActivated.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.red));
        }

        if (state != mLibraryState && state == true) {
            startCountdownTimer();
        } else if (state != mLibraryState && state == false) {
            stopCountdownTimer();
        }
    }

    @Override
    public void onMonitoringStateChanged(TicksStateUpdate ticksStateUpdate) {
        changeTicksMonitoringState(ticksStateUpdate);
    }

    private void changeTicksMonitoringState(TicksStateUpdate ticksStateUpdate) {
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
    public void deliverInternalTick(Location location, Date lastUpdateTime) {
        setInternalTickToUI(location, lastUpdateTime);
    }

    private void setInternalTickToUI(Location location, Date lastUpdateTime) {
        String formattedDate = DateFormat.getTimeInstance().format(lastUpdateTime);
        String value = "<" + location.getLatitude()
                + ", " + location.getLongitude()
                + ", " + formattedDate
                + ">";
        mMainBinding.silentTicks.setText(value);
    }

    @Override
    public void deliverQuietCircleRadiusParameters(double distance, double thresholdRadius) {
        changeQuietCircleRadiusAndDifferenceInDistance(distance, thresholdRadius);
    }

    private void changeQuietCircleRadiusAndDifferenceInDistance(double distance, double thresholdRadius) {
        mMainBinding.thetaCircValue.setText(
                MessageFormat.format("{0}/{1}m", distance, thresholdRadius));
    }

    @Override
    public void onchangeInGoogleStateConnection(GoogleConnectionState state) {

        changeGoogleConnectionState(state);
    }

    private void changeGoogleConnectionState(GoogleConnectionState state) {
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
        mServiceBinderTimerHandler = new Handler();
        mServiceBinderTimerRunnable = new Runnable() {
            @Override
            public void run() {

                changeGoogleConnectionState(mServiceLocationManager.getGoogleConnectionState());
                changeTicksMonitoringState(mServiceLocationManager.getHeartbeatsState());
                changeQuietCircleRadiusAndDifferenceInDistance(
                        mServiceLocationManager.getDistanceOfPhoneCurrentlyFromSilentCircleCentre(),
                        mServiceLocationManager.getSilentCircleThresholdRadius());
                changeLibraryState();

                mCountdownTimerHandler.postDelayed(mCountdownTimerRunnable, UPDATE_UI_DELAY_MILLIS);
            }
        };

        mCountdownTimerHandler = new Handler();
        mCountdownTimerRunnable = new Runnable() {
            @Override
            public void run() {
                Date date = mServiceLocationManager.getLastUpdateTime();
                if (date != null) {
                    long seconds = Utils.getDateDiff(
                            date, Calendar.getInstance(Locale.ENGLISH).getTime(), TimeUnit.SECONDS);
                    mMainBinding.thetaTimeValue.setText(MessageFormat.format("{0}/{1}s",
                            Long.toString(seconds), mServiceLocationManager.getThresholdTime()));
                }

                mCountdownTimerHandler.postDelayed(mCountdownTimerRunnable, 1_000);
            }
        };

    }
}
