package com.example.gclientlib;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 17/01/18.
 */

public interface APIMethods {

    /**
     * starts location monitoring for the client location
     */
    void startLocationMonitoring();

    /**
     * stops location monitoring for the client location
     */
    void stopLocationMonitoring();

    /**
     * returns whether the client is monitoring for location updates or not
     */
    boolean isClientMonitoringLocation();

    /**
     * sets the frequency of receiving location updates of the client
     *
     * @param timeInSeconds time in seconds to be set for receiving location updates
     * @return true if time set successfully
     */
    void setLocationUpdatesFrequency(long timeInSeconds);

    /**
     * returns the Location updates interval
     */
    long getLocationUpdatesFrequency();

    /**
     * start the background service to continue receiving updates even if the app is closed
     */
    void startMonitoringInBackground();

    /**
     * stop the background service
     */
    void stopMonitoringInBackground();

    /**
     * returns whether the client is monitoring for location updates in background or not
     */
    boolean isClientMonitoringInBackground();

    /**
     * determine the source provider of the location whether to receive location
     * updates from NETWORK_PROVIDER or GPS_PROVIDER
     *
     * @param locationSourceProvider  a string determining whether to use ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
     * @return true if the provider set successfully
     */
    void setLocationUpdatesSourceProvider(String locationSourceProvider);

    /**
     * gets the location updates provider
     * @return a string determining whether to use ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
     */
    String getLocationUpdatesSourceProvider();

    /**
     * sets the radius of threshold distance
     * @param distanceInMeters radius of threshold distance in meters
     * @return true if the value set successfully
     */
    void setThresholdRadius(int distanceInMeters);

    /**
     * gets radius of threshold
     */
    int getThresholdRadius();
}
