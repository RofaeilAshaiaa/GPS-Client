package com.kimbrough.gclientlib;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 17/01/18.
 */

interface APIMethods {

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
     * returns the Location updates interval
     */
    long getLocationUpdatesFrequency();

    /**
     * sets the frequency of receiving location updates of the client
     *
     * @param timeInSeconds time in seconds to be set for receiving location updates
     */
    void setLocationUpdatesFrequency(long timeInSeconds);

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
     * gets the location updates provider
     *
     * @return a string determining whether to use ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
     */
    String getLocationUpdatesSourceProvider();

    /**
     * determine the source provider of the location whether to receive location
     * updates from NETWORK_PROVIDER or GPS_PROVIDER
     *
     * @param locationSourceProvider a string determining whether to use ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
     */
    void setLocationUpdatesSourceProvider(String locationSourceProvider);

    /**
     * gets radius of threshold
     */
    int getThresholdRadius();

    /**
     * sets the radius of threshold distance
     *
     * @param distanceInMeters radius of threshold distance in meters
     */
    void setThresholdRadius(int distanceInMeters);

    /**
     * gets threshold time
     *
     * @return time of threshold in seconds
     */
    int getThresholdTime();

    /**
     * sets the time of threshold
     *
     * @param seconds time in seconds
     */
    void setThresholdTime(int seconds);

    /**
     * gets the fastest  frequency interval for receiving location updates
     *
     * @return fastest interval time in seconds
     */
    int getFastestInterval();

    /**
     * sets the fastest  frequency interval for receiving location updates
     */
    void setFastestInterval(int timeSeconds);

    /**
     * gets the priority of the request
     */
    int getPriority();

    /**
     * sets the priority of the request
     */
    void setPriority(int priority);

    void activateLibrary();

    void deactivateLibrary();

    boolean isLibraryActivated();

//    /**
//     * try to reconnect to google location service
//     */
//    void reconnect();
//
//    /**
//     * connect to google location service
//     */
//    void connect();
//
//    /**
//     * disconnect from google location service
//     */
//    void disconnect();

}
