package com.kimbrough.GSFLocation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by usama on 14/12/2017.
 */


class GSGoogleApiClient
{
    public GSGoogleApiClient(){
    }

    public static GoogleApiClient buildGoogleApiClientWithContext(Context context, final GSClientAPIConnectionChangeListener listener)
    {
        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                            @Override
                                            public void onConnected(Bundle bundle) {
                                                Log.d(GSConstants.TAG, "Connected to Google API Client");
                                                listener.onConnectionChange(GSClientAPIConnectionStatus.CONNECTED);
                                            }

                                            @Override
                                            public void onConnectionSuspended(int i) {
                                                Log.d(GSConstants.TAG, "Suspended connection to Google API Client");
                                                listener.onConnectionChange(GSClientAPIConnectionStatus.SUSPENDED);

                                            }
                                        }

                )
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(GSConstants.TAG, "Failed to connect to Google API Client - " + connectionResult.getErrorMessage());
                        listener.onConnectionChange(GSClientAPIConnectionStatus.NOT_CONNECTED);

                    }
                })
                .build();
        return googleApiClient;
    }
}
