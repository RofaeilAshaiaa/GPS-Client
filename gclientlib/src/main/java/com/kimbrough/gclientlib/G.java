package com.kimbrough.gclientlib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 17/01/18.
 */

public class G implements APIMethods {

    public static final String TAG = "GLibrary";
    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x9;
    /**
     * desired interval for location Update interval in milliseconds
     */
    private long mLocationUpdateIntervalMilliseconds;
    /**
     * desired interval for location updates in seconds
     */
    private long mLocationUpdateIntervalSeconds;
    /**
     * determine whether we monitoring for location updates or not
     */
    private boolean mMonitoring;
    /**
     * determine whether we monitoring for location updates in background or not
     */
    private boolean mMonitoringInBackground;
    /**
     * determining whether to use ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
     */
    private String mLocationUpdatesSource;
    /**
     * determining threshold radius
     */
    private int mThresholdRadius;
    /**
     * determine the priority of location updates request
     */
    private int mPriority;
    /**
     * desired fastest interval for location updates in milliseconds
     */
    private int mFastestLocationUpdateIntervalMilliseconds;
    /**
     * desired fastest interval for location updates in seconds
     */
    private int mFastestLocationUpdateIntervalSeconds;
    /**
     * determining threshold time which will be used to set up a timer
     */
    private int mThresholdTime = 10;
    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;
    /**
     * determine whether the library activated and in use or not
     */
    private boolean mIsLibraryActivated;
    /**
     * mListenerGClient to receive location updates from our library
     */
    private LocationListenerGClient mListenerGClient;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation = null;
    /**
     * new location received
     */
    private Location mNewLocation = null;
    /**
     * state of current seconds in the timer
     */
    private int mTimer;
    private Handler mHandler;
    private Runnable mRunnable;


    public G(final LocationListenerGClient listenerGClient) {

        this.mListenerGClient = listenerGClient;

    }

    @Override
    public void activateLibrary() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient((Activity) mListenerGClient);
        mSettingsClient = LocationServices.getSettingsClient((Activity) mListenerGClient);
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        mIsLibraryActivated = true;
        //
        startLocationMonitoring();
        mListenerGClient.onLibraryStateChanged();
    }

    @Override
    public void deactivateLibrary() {
        mIsLibraryActivated = false;
        stopLocationMonitoring();
        stopTimer();
        mListenerGClient.onLibraryStateChanged();
    }

    @Override
    public boolean isLibraryActivated() {
        return mIsLibraryActivated;
    }

    @Override
    public void startLocationMonitoring() {
        startLocationUpdates();
        mMonitoring = true;
        mListenerGClient.onMonitoringStateChanged();
    }

    @Override
    public void stopLocationMonitoring() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mMonitoring = false;
        mListenerGClient.onMonitoringStateChanged();
    }

    @Override
    public boolean isClientMonitoringLocation() {
        return mMonitoring;
    }

    @Override
    public long getLocationUpdatesFrequency() {
        return mLocationUpdateIntervalSeconds;
    }

    @Override
    public void setLocationUpdatesFrequency(long timeInSeconds) {
        mLocationUpdateIntervalSeconds = timeInSeconds;
        mLocationUpdateIntervalMilliseconds = timeInSeconds * 1_000;
        //start location monitoring with the new settings
        if (mIsLibraryActivated && (mMonitoring || mMonitoringInBackground)) {
            // construct the location request with the new parameters
            createLocationRequest();
            buildLocationSettingsRequest();
            startLocationMonitoring();
        }
    }

    @Override
    public void startMonitoringInBackground() {
        mMonitoringInBackground = true;
    }

    @Override
    public void stopMonitoringInBackground() {
        mMonitoringInBackground = false;
    }

    @Override
    public boolean isClientMonitoringInBackground() {
        return mMonitoringInBackground;
    }

    @Override
    public String getLocationUpdatesSourceProvider() {
        return mLocationUpdatesSource;
    }

    @Override
    public void setLocationUpdatesSourceProvider(String locationSourceProvider) {
        mLocationUpdatesSource = locationSourceProvider;
    }

    @Override
    public int getThresholdRadius() {
        return mThresholdRadius;
    }

    @Override
    public void setThresholdRadius(int distanceInMeters) {
        mThresholdRadius = distanceInMeters;
    }

    @Override
    public int getThresholdTime() {
        return mThresholdTime;
    }

    @Override
    public void setThresholdTime(int seconds) {
        mThresholdTime = seconds;
    }

    @Override
    public int getFastestInterval() {
        return mFastestLocationUpdateIntervalSeconds;
    }

    @Override
    public void setFastestInterval(int timeSeconds) {
        mFastestLocationUpdateIntervalSeconds = timeSeconds;
        mFastestLocationUpdateIntervalMilliseconds = timeSeconds * 1_000;

//        //start location monitoring with the new settings
//        if (mIsLibraryActivated && (mMonitoring || mMonitoringInBackground)) {
//            // construct the location request with the new parameters
//            createLocationRequest();
//            buildLocationSettingsRequest();
//            startLocationMonitoring();
//        }
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public void setPriority(int priority) {
        mPriority = priority;
        //start location monitoring with the new settings
        if (mIsLibraryActivated && (mMonitoring || mMonitoringInBackground)) {
            // construct the location request with the new parameters
            createLocationRequest();
            buildLocationSettingsRequest();
            startLocationMonitoring();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(mLocationUpdateIntervalMilliseconds);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(mFastestLocationUpdateIntervalMilliseconds);

        mLocationRequest.setPriority(mPriority);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (mCurrentLocation == null) {
                    //this means this is the first location we received
                    mCurrentLocation = locationResult.getLastLocation();
                    sendLocationAndTime();
                    createHandlerAndRunnable();
                    resetTimer();
                } else {
                    //we already received a previous location,
                    //we need to make sure that the new location should be broad-casted or not
                    mNewLocation = locationResult.getLastLocation();
                    double distance = Utils.distanceInKmBetweenEarthCoordinates(
                            mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            mNewLocation.getLatitude(), mNewLocation.getLongitude());

                    if (distance > mThresholdRadius) {
                        // this means that the client have moved out of the radius we determined
                        mCurrentLocation = mNewLocation;
                        sendLocationAndTime();
                        resetTimer();
                    } else {
                        //shows silent consuming of ticks with toast
                        Toast.makeText((Context) mListenerGClient, "New Tick Consumed Silently", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                boolean isLocationAvailable = locationAvailability.isLocationAvailable();
                if (isLocationAvailable)
                    mListenerGClient.onLocationAvailabilityChanged(ConnectionState.CONNECTED);
                else
                    mListenerGClient.onLocationAvailabilityChanged(ConnectionState.DISCONNECTED);
            }
        };
    }

    private void resetTimer() {
        mHandler.removeCallbacks(mRunnable);
        mTimer = 0;
        mHandler.postDelayed(mRunnable, 1_000);
    }

    private void stopTimer() {
        mHandler.removeCallbacks(mRunnable);
        mTimer = 0;
    }


    private void createHandlerAndRunnable() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // if the we reached the threshold time we should
                // send the location and reset the timer
                if (mTimer == mThresholdTime) {
                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    mListenerGClient.newLocationUpdateReceived(mCurrentLocation, mLastUpdateTime);
                    resetTimer();
                } else {
                    // we should increase the timer by 1 and run it after 1 second
                    mTimer++;
                    mHandler.postDelayed(this, 1_000);
                }
            }
        };
    }

    private void sendLocationAndTime() {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mListenerGClient.newLocationUpdateReceived(mCurrentLocation, mLastUpdateTime);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener((Activity) mListenerGClient, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission((Context) mListenerGClient, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission((Context) mListenerGClient, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener((Activity) mListenerGClient, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult((Activity) mListenerGClient, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText((Context) mListenerGClient, errorMessage, Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

}
