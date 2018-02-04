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
import java.util.ArrayList;
import java.util.Date;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

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
    private long mLocationUpdateIntervalMilliseconds = 1_000;
    /**
     * desired interval for location updates in seconds
     */
    private long mLocationUpdateIntervalSeconds = 1;
    /**
     * determine whether we monitoring for location updates or not
     */
    private boolean mMonitoring;
    /**
     * determine whether we monitoring for location updates in background or not
     */
    private boolean mMonitoringInBackground;
    /**
     * determine the priority of location updates request
     */
    private int mPriority = PRIORITY_HIGH_ACCURACY;
    /**
     * desired fastest interval for location updates in milliseconds
     */
    private int mFastestLocationUpdateIntervalMilliseconds = 1_000;
    /**
     * desired fastest interval for location updates in seconds
     */
    private int mFastestLocationUpdateIntervalSeconds = 1;
    /**
     * a time in seconds after which a broadcast of the gps triplet <lat,long,datetime> will occur even if phone is not
     * moving much.  be default 5 minutes
     */
    private int mThresholdTime = 300;
    /**
     * a distance in metres which constitutes an ongoing circle radius based on the location of the phone at some
     * arbitrary point in time T earlier.  if the phone hasn't moved currently further than this radius, then it
     * is deemed to be, for the purposes of the library, in the same place, and a consequence of this is that
     * the gps triplets are not externally broadcast from the library
     */
    private double mThresholdRadius = 5;
    /**
     * Time when the location was updated via the Google location API. Represented as a String.
     */
    private String mLastUpdateTime;
    /**
     * State variable indicating if the library is switched on and ready for use
     */
    private boolean mIsLibraryActivated;
    /**
     * mListenerGClient to receive location updates from Google loctation API
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
     * Represents a geographical location corresponding to where this Android phone is.
     */
    private Location mCurrentLocation = null;
    /**
     * newest location received from Google is stored in this variable
     */
    private Location mNewLocation = null;
    private Handler mHandler;
    private Runnable mRunnable;
    // list of locations until we rest the timer
    private ArrayList<Location> mLocationArrayList;
    //determines whether we are connected and location updates available
    private ConnectionState mConnectionState;
    // determines the difference between latest new location and a baseline location of this android
    // phone from some period earlier
    private double mDistance = 0;


    public G(final LocationListenerGClient listenerGClient) {

        this.mListenerGClient = listenerGClient;
        mLocationArrayList = new ArrayList<>();
        createHandlerAndRunnable();
    }

    @Override
    public void activateLibrary() {
        mIsLibraryActivated = true;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient((Activity) mListenerGClient);
        mSettingsClient = LocationServices.getSettingsClient((Activity) mListenerGClient);
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationMonitoring();
        mListenerGClient.onLibraryStateChanged();
    }

    @Override
    public void deactivateLibrary() {
        stopLocationMonitoring();
        stopTimer();
        mIsLibraryActivated = false;
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
    public void restartLocationMonitoring() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        buildLocationSettingsRequest();
        startLocationUpdates();
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
            // restart location monitoring with the new parameters
            mLocationRequest.setInterval(mLocationUpdateIntervalMilliseconds);
            restartLocationMonitoring();
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
    public double getThresholdRadius() {
        return mThresholdRadius;
    }

    @Override
    public void setThresholdRadius(double distanceInMeters) {
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
            // restart location monitoring with the new parameters
            mLocationRequest.setPriority(mPriority);
            restartLocationMonitoring();
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
                    mLocationArrayList.add(mCurrentLocation);
                    //sets the first mDistance with zero value of threshold mDistance
                    sendLocationAndTime();
                    deliverThetaDistance(mDistance);
                    deliverThetaTime();
                    startTimer();
                } else {
                    //we already received a previous location,
                    //we need to make sure that the new location should be broad-casted or not
                    mNewLocation = locationResult.getLastLocation();
                    mDistance = 1000 * Utils.distanceInKmBetweenEarthCoordinates(
                            mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                            mNewLocation.getLatitude(), mNewLocation.getLongitude());
                    mLocationArrayList.add(mNewLocation);

                    if (mDistance > mThresholdRadius) {
                        // this means that the client have moved out of the radius we determined
                        mCurrentLocation = mNewLocation;
                        sendLocationAndTime();
                        stopTimer();
                        startTimer();
                    } else {
                        //deliver silent consuming of ticks
                        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                        mListenerGClient.deliverSilentTick(mNewLocation, mLastUpdateTime);
                    }
                    deliverThetaDistance(mDistance);
                }
                deliverThetaTime();
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                boolean isLocationAvailable = locationAvailability.isLocationAvailable();
                if (isLocationAvailable){
                    mConnectionState = ConnectionState.CONNECTED;
                    mListenerGClient.onLocationAvailabilityChanged(mConnectionState);
                }
                else{
                    mConnectionState = ConnectionState.DISCONNECTED;
                    mListenerGClient.onLocationAvailabilityChanged(mConnectionState);
                }
            }
        };
    }

    private void deliverThetaDistance(double distance) {
        mListenerGClient.deliverThetaDistance(distance, mThresholdRadius);
    }

    private void deliverThetaTime() {
        //deliver time with zero because we had to revert to previous
        // implementation of the timer i.e. timer should fires every second
        mListenerGClient.deliverThetaTime(0, mThresholdTime);
    }

    /**
     * starts the timer with the value of threshold time as seconds
     */
    private void startTimer() {
        mHandler.postDelayed(mRunnable, mThresholdTime * 1000);
    }

    /**
     * stop the current timer if running and empty the list of locations we have
     */
    private void stopTimer() {
        mHandler.removeCallbacks(mRunnable);
        mLocationArrayList.clear();
    }

    private void createHandlerAndRunnable() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // if the we reached the threshold time we should
                // send the location and reset the timer
                sendLocationAndTime();
                deliverThetaDistance(mDistance);
                deliverThetaTime();
                mLocationArrayList.add(mCurrentLocation);
                startTimer();
            }
        };
    }

    private void sendLocationAndTime() {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mListenerGClient.deliverNewLocationUpdate(mCurrentLocation, mLastUpdateTime);
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
