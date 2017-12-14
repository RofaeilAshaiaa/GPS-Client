package com.kimbrough.GSFLocation;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Created by Usama on 13/12/2017.
 */

public class GSLocationManager {

    private static GSLocationManager m_instance = null;
    public GSClientAPIConnectionStatus m_connectionStatus = GSClientAPIConnectionStatus.NOT_CONNECTED;
    private ArrayList<GSLocationChangeListener> m_observers;
    private GoogleApiClient m_googleApiClient = null;

    public GSLocationManager() {
        m_observers = new ArrayList<GSLocationChangeListener>();
    }
    public static GSLocationManager getInstance() {

        if (m_instance == null) {
            m_instance = new GSLocationManager();
        }
        return m_instance;
    }


    public void startLocationMonitoring(final Activity activity)
    {
        m_googleApiClient = GSGoogleApiClient.buildGoogleApiClientWithContext(activity, new GSClientAPIConnectionChangeListener() {
            @Override
            public void onConnectionChange(GSClientAPIConnectionStatus status) {
                if (status == GSClientAPIConnectionStatus.CONNECTED)
                {
                    final LocationRequest locationRequest = GSLocationRequest.getLocationRequest(activity);

                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(m_googleApiClient,
                                locationRequest, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location)
                                    {
                                        Log.i(GSConstants.TAG, "Loc Update: Lat/Long = " + location.getLatitude()  + " " + location.getLongitude());
                                        for (GSLocationChangeListener observer: m_observers) {
                                            observer.onLocationChange(location);
                                        }
                                    }

                                });
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        connectClient();

    }

    public  void connectClient()
    {
        m_googleApiClient.connect();
    }
    public void  disconnectClient()
    {
        m_googleApiClient.disconnect();
    }

    public void getLastKnowLocation(Activity activity)
    {
        try {
            FusedLocationProviderClient mFusedLocationClient;
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.i(GSConstants.TAG, "Last Location: Lat/Long = " + location.getLatitude()  + " " + location.getLongitude());
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void addLocationChangeListener(GSLocationChangeListener listener)
    {
        m_observers.add(listener);
    }

    public void removeLocationChangeListener(GSLocationChangeListener listener)
    {
        m_observers.remove(listener);
    }
}
