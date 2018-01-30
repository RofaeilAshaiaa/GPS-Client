package com.kimbrough.gclientlib;

import android.location.Location;
import android.location.LocationManager;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.location.LocationRequest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {

        G guser = new G(new LocationListenerGClient() {
            @Override
            public void deliverNewLocationUpdate(Location location, String mLastUpdateTime) {

            }

            @Override
            public void onLocationAvailabilityChanged(ConnectionState state) {

            }

            @Override
            public void onLibraryStateChanged() {

            }

            @Override
            public void onMonitoringStateChanged() {

            }

            @Override
            public void deliverSilentTick(Location location, String lastUpdateTime) {

            }
        });

        guser.setLocationUpdatesFrequency(20_000);
        assertEquals(20_000, guser.getLocationUpdatesFrequency());

        guser.setLocationUpdatesSourceProvider(LocationManager.NETWORK_PROVIDER);
        assertEquals(LocationManager.NETWORK_PROVIDER, guser.getLocationUpdatesSourceProvider());

        guser.setThresholdRadius(30);
        assertEquals(30, guser.getThresholdRadius());


        guser.startMonitoringInBackground();
        assertTrue(guser.isClientMonitoringInBackground());
        guser.stopMonitoringInBackground();
        assertFalse(guser.isClientMonitoringInBackground());

        guser.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        assertEquals(LocationRequest.PRIORITY_HIGH_ACCURACY, guser.getPriority());

        guser.setThresholdTime(20);
        assertEquals(20, guser.getThresholdTime());


        guser.setFastestInterval(100);
        assertEquals(100, guser.getFastestInterval());

    }
}
