package com.kimbrough.gclientlib;

import android.location.Location;

import com.kimbrough.gclientlib.helper.GoogleConnectionState;
import com.kimbrough.gclientlib.helper.TicksStateUpdate;

import java.util.Date;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 20/01/18.
 */

public interface LocationListenerGClient {

    void deliverBroadcastLocationUpdate(Location location, Date mLastUpdateTime);

    void onLibraryStateChanged();

    void onMonitoringStateChanged(TicksStateUpdate ticksStateUpdate);

    void deliverInternalTick(Location location, Date lastUpdateTime);

    void deliverQuietCircleRadiusParameters(double distance, double thresholdRadius);

    void onchangeInGoogleStateConnection(GoogleConnectionState state);
}
