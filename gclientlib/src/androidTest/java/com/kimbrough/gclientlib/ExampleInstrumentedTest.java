package com.kimbrough.gclientlib;

import android.location.Location;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.location.LocationRequest;
import com.kimbrough.gclientlib.helper.GoogleConnectionState;
import com.kimbrough.gclientlib.helper.TicksStateUpdate;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

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
            public void deliverBroadcastLocationUpdate(Location location, Date mLastUpdateTime) {

            }


            @Override
            public void onLibraryStateChanged() {

            }

            @Override
            public void onMonitoringStateChanged(TicksStateUpdate ticksStateUpdate) {

            }

            @Override
            public void deliverInternalTick(Location location, Date lastUpdateTime) {

            }

            @Override
            public void deliverQuietCircleRadiusParameters(double distance, double thresholdRadius) {

            }

            @Override
            public void onchangeInGoogleStateConnection(GoogleConnectionState state) {

            }


        });

        guser.setLocationUpdatesFrequency(20_000);
        assertEquals(20_000, guser.getLocationUpdatesFrequency());

        guser.setThresholdRadius(30);
        assertEquals(30, guser.getThresholdRadius(), 0);


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
