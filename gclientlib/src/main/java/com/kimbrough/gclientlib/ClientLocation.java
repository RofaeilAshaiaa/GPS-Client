package com.kimbrough.gclientlib;

import android.location.Location;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 26/01/18.
 */

public class ClientLocation {
    private Location location;
    private long date;

    public ClientLocation() {
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
