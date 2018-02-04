package com.kimbrough.gclientlib;

import android.location.Location;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 20/01/18.
 */

public interface LocationListenerGClient {

    void deliverNewLocationUpdate(Location location, String mLastUpdateTime);

    void onLocationAvailabilityChanged(ConnectionState state);

    void onLibraryStateChanged();

    void onMonitoringStateChanged();

    void deliverSilentTick(Location location, String lastUpdateTime);

    void deliverQuietCircleExpiryParameter(int timerTime, int thresholdTime);

    void deliverQuietCircleRadiusParameters(double distance, double thresholdRadius);

}
