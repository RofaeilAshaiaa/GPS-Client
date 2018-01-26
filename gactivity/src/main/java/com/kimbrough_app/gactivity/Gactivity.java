package com.kimbrough_app.gactivity;

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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class Gactivity extends AppCompatActivity {

    public static final String TAG = "Gactivity";
    public static final String GEOFENCE_ID = "mygeofenceid";
    public static final int REQUEST_CHECK_SETTINGS = 1;
    GoogleApiClient googleApiClient = null;

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
                startLocationMonitoring();
            }
        });

        startGeofenceMonitoring = (Button)findViewById(R.id.startGeofenceMonitoring);
        startGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGeofenceMonitoring();
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                            @Override
                                            public void onConnected(Bundle bundle) {
                                                Log.i(TAG, "Connected to Google API Client");
                                            }

                                            @Override
                                            public void onConnectionSuspended(int i) {
                                                Log.i(TAG, "Suspended connection to Google API Client");
                                            }
                                        }

                )
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.i(TAG, "Failed to connect to Google API Client - " + connectionResult.getErrorMessage());
                    }
                })
                .build();

        requestPermissions(new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},  12345);
    }

    void startLocationMonitoring()
    {
        Log.i(TAG, "Monitoring Started");
        final LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(2000)
                //.setNumUpdates(5)
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(Gactivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    locationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location)
                        {
                            Log.i(TAG, "Loc Update: Lat/Long = " + location.getLatitude()  + " " + location.getLongitude());
                            latLongDisplay.setText(location.getLatitude() + ", " + location.getLongitude(), TextView.BufferType.SPANNABLE);
                        }

                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        try {
            FusedLocationProviderClient mFusedLocationClient;
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.i(TAG, "Last Location: Lat/Long = " + location.getLatitude()  + " " + location.getLongitude());
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
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
        googleApiClient.reconnect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        googleApiClient.disconnect();
    }
}
