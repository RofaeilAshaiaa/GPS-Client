package com.kimbrough.gclientlib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.kimbrough.gclientlib.LocationUpdatesForegroundService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 19/02/18.
 */

public class Utils {

    public static final String PACKAGE_NAME = "com.kimbrough.gclientlib";
    public static final String ACTION_BROADCAST_LOCATION = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_TYPE_OF_TICK = PACKAGE_NAME + ".type.of.tick";
    public static final String EXTRA_DATE = PACKAGE_NAME + ".date";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";

    /**
     * Get a diff between two dates
     * reference link:
     * https://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances
     *
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * initialize the library, through starting a service
     * before activate it to connect the activity to it later.
     *
     * @param activity          activity that the service will be bind to it
     * @param serviceConnection {@link ServiceConnection} object that will handle the IBinder
     *                          object when onBind() in  the service called
     *                          to bind to the activity
     */
    public static void initializeLibrary(Activity activity, ServiceConnection serviceConnection) {

        Intent intent = new Intent(activity,
                LocationUpdatesForegroundService.class);
        activity.startService(intent);
        // Bind to the service
        activity.bindService(intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE);

    }

    public interface LocationTickType {

        String BROADCAST_TICK = "broadcast";
        String INTERNAL_TICK = "internal";
    }
}
