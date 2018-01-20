package com.example.gclientlib;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 17/01/18.
 */

public class G implements APIMethods {

    public static final String TAG = "GLibrary";
    public static final int REQUEST_CHECK_SETTINGS = 1;
    /**
     * desired interval for location Update interval in milliseconds
     */
    private long mLocationUpdateIntervalMilliseconds = 10000;
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
    private int mPriority;
    /**
     * desired fastest interval for location updates in seconds
     */
    private int mFastestLocationUpdateIntervalSeconds;
    private int mThresholdTime;
    private boolean mIsLibraryActivated;
    private GoogleApiClient googleApiClient = null;
    /**
     * mListenerGClient to receive location updates from our library
     */
    private LocationListenerGClient mListenerGClient;

    /**
     * determine whether the Location Settings are Satisfied or not
     */
    private boolean mAreLocationSettingsSatisfied;

    /**
     * define location request object
     */
    private LocationRequest mLocationRequest;

    /**
     * last known location of the client
     */
    private Location mLastKnownLocation;

    /**
     * callbacks to location updates
     */
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private ConnectionState mConnectionState;

    public G(final LocationListenerGClient listenerGClient) {

        this.mListenerGClient = listenerGClient;

        googleApiClient = new GoogleApiClient.Builder((Context) listenerGClient)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                            @Override
                                            public void onConnected(Bundle bundle) {
                                                Log.i(TAG, "Connected to Google API Client");
                                                mConnectionState = ConnectionState.CONNECTED;
                                            }

                                            @Override
                                            public void onConnectionSuspended(int i) {
                                                Log.i(TAG, "Suspended connection to Google API Client");
                                                mConnectionState = ConnectionState.DISCONNECTED;
                                            }
                                        }

                )
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.i(TAG, "Failed to connect to Google API Client - " + connectionResult.getErrorMessage());
                        mConnectionState = ConnectionState.CONNECTION_Failed;
                    }
                })
                .build();

    }

    @Override
    public void startLocationMonitoring() {

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    mLocationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i(TAG, "Loc Update: Lat/Long = " + location.getLatitude() + " " + location.getLongitude());
                            mListenerGClient.newLocationUpdateReceived(location);
                        }

                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        mMonitoring = true;
    }

    @Override
    public void stopLocationMonitoring() {

        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mMonitoring = false;

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
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public void setPriority(int priority) {
        mPriority = priority;
        createLocationRequest();
    }

    @Override
    public boolean activateLibrary() {

        try {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient((Activity) mListenerGClient);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener((Activity) mListenerGClient, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            mIsLibraryActivated = true;
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mLastKnownLocation = location;
                                Log.i(TAG, "Last Location: Lat/Long = " + location.getLatitude()  + " " + location.getLongitude());
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
            mIsLibraryActivated =  false;
        }

        return mIsLibraryActivated;
    }

    @Override
    public boolean deactivateLibrary() {
        mIsLibraryActivated = false;
        return mIsLibraryActivated;
    }

    @Override
    public void reconnect() {
        googleApiClient.reconnect();
    }

    @Override
    public void connect() {
        googleApiClient.connect();
    }

    @Override
    public void disconnect() {
        googleApiClient.disconnect();
    }

    private void createLocationRequest() {

        mLocationRequest = LocationRequest.create()
                .setInterval(mLocationUpdateIntervalSeconds)
                .setFastestInterval(mFastestLocationUpdateIntervalSeconds)
                //.setNumUpdates(5)
                .setPriority(mPriority);
    }

    private void areLocationRequestSettingsSatisfied() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient((Activity) mListenerGClient);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener((Activity) mListenerGClient, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                mAreLocationSettingsSatisfied = true;
            }
        });

        task.addOnFailureListener((Activity) mListenerGClient, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            mAreLocationSettingsSatisfied = false;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult((Activity) mListenerGClient,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

}
