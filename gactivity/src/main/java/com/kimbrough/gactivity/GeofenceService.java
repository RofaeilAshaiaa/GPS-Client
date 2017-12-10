package com.kimbrough.gactivity;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by john on 09/07/2017.
 */

public class GeofenceService extends IntentService {
    public static final String TAG = "GeofenceService";




    public GeofenceService()
    {
        super(TAG);
        Log.d(TAG, "Geofencing service ctor called");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(TAG, "We got a Geofence event");
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError())
        {
            // todo handle error
        }
        else
        {
            int transition = event.getGeofenceTransition();
            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);
            String requestId = geofence.getRequestId();

            if ( transition == Geofence.GEOFENCE_TRANSITION_ENTER)
            {
                Log.d(TAG, "Entering Geofence " + requestId);
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneG.startTone(ToneGenerator.TONE_DTMF_0, 200);
            }
            else if (transition ==  Geofence.GEOFENCE_TRANSITION_EXIT)
            {
                Log.d(TAG, "Exiting Geofence " + requestId);
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneG.startTone(ToneGenerator.TONE_DTMF_S, 400);
            }
        }
    }
}
