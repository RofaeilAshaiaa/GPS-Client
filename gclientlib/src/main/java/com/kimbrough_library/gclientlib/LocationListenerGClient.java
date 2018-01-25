package com.kimbrough_library.gclientlib;

import android.location.Location;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 20/01/18.
 */

public interface LocationListenerGClient {

    void newLocationUpdateReceived(Location location, String mLastUpdateTime);

    void onLocationAvailabilityChanged(ConnectionState state);

    void onLibraryStateChanged();

    void onMonitoringStateChanged();

}
