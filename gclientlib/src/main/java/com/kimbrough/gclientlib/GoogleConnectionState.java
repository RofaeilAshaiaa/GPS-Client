package com.kimbrough.gclientlib;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 04/02/18.
 */

public enum GoogleConnectionState {
    //we haven't even asked Google anything
    NEW_UNCONNECTED_SESSION,
    //google tells me it is now connected
    CONNECTED_TO_GOOGLE_LOCATION_API,
    //google tells us it has a problem
    GOOGLE_UPDATE_UNAVAILABLE,
    //we couldn't complete the client connect because of this Google error
    GOOGLE_INIT_CONNECT_FAILURE_TO_ESTABLISH_CONNECTION,
    //we chose to deactivate the library
    DISCONNECTED_GOOGLE_API
}
