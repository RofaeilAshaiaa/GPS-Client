package com.example.gclientlib;

import android.location.LocationManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 17/01/18.
 */

public class GLibraryUnitTest {


    @Test
    public void settersAndGettersTest() throws Exception {

        G guser = new G();

        guser.setLocationUpdatesFrequency(20_000);
        assertEquals(20_000, guser.getLocationUpdatesFrequency());

        guser.setLocationUpdatesSourceProvider(LocationManager.NETWORK_PROVIDER);
        assertEquals(LocationManager.NETWORK_PROVIDER, guser.getLocationUpdatesSourceProvider());

        guser.setThresholdRadius(30);
        assertEquals(30, guser.getThresholdRadius());

        guser.startLocationMonitoring();
        assertTrue(guser.isClientMonitoringLocation());
        guser.stopLocationMonitoring();
        assertFalse(guser.isClientMonitoringLocation());

        guser.startMonitoringInBackground();
        assertTrue(guser.isClientMonitoringInBackground());
        guser.stopMonitoringInBackground();
        assertFalse(guser.isClientMonitoringInBackground());
    }
}