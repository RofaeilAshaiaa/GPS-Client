package com.kimbrough.gclientlib;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.kimbrough.gclientlib.helper.GoogleConnectionState;
import com.kimbrough.gclientlib.helper.TicksStateUpdate;
import com.kimbrough.gclientlib.utils.Utils;

import java.util.Date;

/**
 * @author Rofaeil Ashaiaa
 *         Created on 06/03/18.
 */

public class LocationUpdatesForegroundService extends Service implements LocationListenerGClient {

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 123456789;

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    private static final String TAG = LocationUpdatesForegroundService.class.getSimpleName();

    // Binder given to clients when they bind to this service
    private final IBinder mBinder = new LocalBinder();

    private NotificationManager mNotificationManager;

    private G mLocationManager;

    /**
     * The current location.
     */
    private Location mLocation;

    private Handler mServiceHandler;

    private Activity mBindActivity;


    @Override
    public void onCreate() {

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        mLocationManager = new G(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(NOTIFICATION_ID, getNotification());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //return true if you want onRebind() to be called when a client re-binds.
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void deliverBroadcastLocationUpdate(Location location, Date lastUpdateTime) {
        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(Utils.ACTION_BROADCAST_LOCATION);
        intent.putExtra(Utils.EXTRA_LOCATION, location);
        intent.putExtra(Utils.EXTRA_DATE, lastUpdateTime.getTime());
        intent.putExtra(Utils.EXTRA_TYPE_OF_TICK, Utils.LocationTickType.BROADCAST_TICK);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    }

    @Override
    public void onLibraryStateChanged() {

    }

    @Override
    public void onMonitoringStateChanged(TicksStateUpdate ticksStateUpdate) {

    }

    @Override
    public void deliverInternalTick(Location location, Date lastUpdateTime) {
        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(Utils.ACTION_BROADCAST_LOCATION);
        intent.putExtra(Utils.EXTRA_LOCATION, location);
        intent.putExtra(Utils.EXTRA_DATE, lastUpdateTime.getTime());
        intent.putExtra(Utils.EXTRA_TYPE_OF_TICK, Utils.LocationTickType.INTERNAL_TICK);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void deliverQuietCircleRadiusParameters(double distance, double thresholdRadius) {
    }

    @Override
    public void onchangeInGoogleStateConnection(GoogleConnectionState state) {

    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {

        CharSequence contentText = "content text";
        CharSequence contentTitle = "contentTitle";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(contentText)
                .setContentTitle(contentTitle)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setTicker(contentText)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    public void stopForeground() {
        stopForeground(true);
        stopSelf();
    }

    public boolean isLibraryActivated() {
        return mLocationManager.isLibraryActivated();
    }

    public void setFastestInterval(int intervalInSeconds) {
        mLocationManager.setFastestInterval(intervalInSeconds);
    }

    public void setLocationUpdatesFrequency(int intervalInSeconds) {
        mLocationManager.setLocationUpdatesFrequency(intervalInSeconds);
    }

    public void setThresholdRadius(double distanceValue) {
        mLocationManager.setThresholdRadius(distanceValue);
    }

    public Date getLastUpdateTime() {
        return mLocationManager.getLastUpdateTime();
    }

    public int getThresholdTime() {
        return mLocationManager.getThresholdTime();
    }

    public void setThresholdTime(int thresholdTime) {
        mLocationManager.setThresholdTime(thresholdTime);
    }

    public void setPriority(int priority) {
        mLocationManager.setPriority(priority);
    }

    public void activateLibrary(Activity activity) {
        mLocationManager.activateLibrary(activity);
    }

    public void deactivateLibrary() {
        mLocationManager.deactivateLibrary();
        stopForeground();
    }

    public GoogleConnectionState getGoogleConnectionState() {
        return mLocationManager.getGoogleConnectionState();
    }

    public TicksStateUpdate getHeartbeatsState() {
        return mLocationManager.getHeartbeatsState();
    }

    public double getDistanceOfPhoneCurrentlyFromSilentCircleCentre() {
        return mLocationManager.getDistanceOfPhoneCurrentlyFromSilentCircleCentre_metres();
    }

    public double getSilentCircleThresholdRadius() {
        return mLocationManager.getSilentCircleThresholdRadius();
    }

    /**
     * Class used for the client Binder.
     */
    public class LocalBinder extends Binder {
        public LocationUpdatesForegroundService getService() {
            return LocationUpdatesForegroundService.this;
        }
    }
}
