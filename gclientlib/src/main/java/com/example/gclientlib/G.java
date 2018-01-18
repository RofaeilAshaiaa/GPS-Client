package com.example.gclientlib;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 17/01/18.
 */

public class G implements APIMethods {


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

    private String mPriority;

    /**
     * desired fastest interval for location updates in seconds
     */
    private int mFastestLocationUpdateIntervalSeconds;

    private int mThresholdTime;

    private boolean mIsLibraryActivated;

    @Override
    public void startLocationMonitoring() {
        mMonitoring = true;
    }

    @Override
    public void stopLocationMonitoring() {
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
    public String getPriority() {
        return mPriority;
    }

    @Override
    public void setPriority(String priority) {
        mPriority = priority;
    }

    @Override
    public boolean activateLibrary() {

        mIsLibraryActivated = true;
        return mIsLibraryActivated;
    }

    @Override
    public boolean deactivateLibrary() {
        mIsLibraryActivated = false;
        return mIsLibraryActivated;
    }

    @Override
    public void reconnect() {

    }

}
