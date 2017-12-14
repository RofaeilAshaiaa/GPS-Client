package com.kimbrough.gactivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.kimbrough.GSFLocation.GSLocationChangeListener;
import com.kimbrough.GSFLocation.GSLocationManager;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class Gactivity extends AppCompatActivity implements GSLocationChangeListener {

    public static final String TAG = "Gactivity";
    public static final String GEOFENCE_ID = "mygeofenceid";
    GoogleApiClient googleApiClient = null;
    private GSLocationManager m_gsfLocationManager = null;

    // my computer
    double b_lat = 51.4773982; // betsy 51.4773598;
    double b_long = 0.0729307;// betsy 0.072883;


    private Button startLocationMonitoring;
    private Button startGeofenceMonitoring;
    private Button stopGeofenceMonitoring;
    private EditText latLongDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gactivity);

        latLongDisplay = (EditText)findViewById(R.id.LatLong);
        latLongDisplay.setText("Lat & Long", TextView.BufferType.SPANNABLE);
        startLocationMonitoring = (Button)findViewById(R.id.startLocationMonitoring);
        startLocationMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startLocationMonitoring();
                m_gsfLocationManager.startLocationMonitoring(Gactivity.this);

            }
        });

        startGeofenceMonitoring = (Button)findViewById(R.id.startGeofenceMonitoring);
        startGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startGeofenceMonitoring();
            }
        });
        m_gsfLocationManager = GSLocationManager.getInstance();
        //m_gsfLocationManager.startLocationMonitoring(this);
        m_gsfLocationManager.addLocationChangeListener(new GSLocationChangeListener() {
            @Override
            public void onLocationChange(Location location) {
                latLongDisplay.setText(location.getLatitude() + ", " + location.getLongitude(), TextView.BufferType.SPANNABLE);
            }
        });


        requestPermissions(new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},  12345);
    }

    void startGeofenceMonitoring()
    {
        Log.i(TAG, "starting a geofence");
        Geofence geofence = new Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(b_lat,b_long,1)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // if you're already in the geofence by here, then it will fire right away
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(this, GeofenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (!googleApiClient.isConnected() )
        {
            Log.d(TAG,"Google API Client is not connected");
        }
        else
        {
            try {
                LocationServices.GeofencingApi.addGeofences(googleApiClient,geofencingRequest,pendingIntent)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if ( status.isSuccess())
                                {
                                    Log.d(TAG, "Successfully added geofence");
                                }
                                else
                                {
                                    Log.d(TAG, "Failed to add add geofence:" + status.getStatus());
                                }
                            }
                        });
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }
    void stopGeofenceMonitoring()
    {
        Log.d(TAG, "Stop monitoring is called");
        ArrayList<String> geofenceIds = new ArrayList<String>();
        geofenceIds.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient,geofenceIds);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //googleApiClient.reconnect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //googleApiClient.disconnect();
        m_gsfLocationManager.disconnectClient();
    }

    @Override
    public void onLocationChange(Location location) {
        latLongDisplay.setText(location.getLatitude() + ", " + location.getLongitude(), TextView.BufferType.SPANNABLE);

    }
}
