package com.example.gclientlib;

import android.location.Location;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 20/01/18.
 */

public interface LocationListenerGClient {

    void newLocationUpdateReceived(Location location);

    void onError(int error);

}
