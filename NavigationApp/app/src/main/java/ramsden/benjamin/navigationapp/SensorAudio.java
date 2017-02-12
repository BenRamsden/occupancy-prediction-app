package ramsden.benjamin.navigationapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorAudio {

    SensorAudioAsyncTask sensorAudioAsyncTask;

    public SensorAudio(Context context) {
        // Record to the external cache directory for visibility
        sensorAudioAsyncTask = new SensorAudioAsyncTask();

    }

    public void start() {
        Log.d(Constants.SENSOR_AUDIO, "startScan");

        if(sensorAudioAsyncTask.getStatus() == AsyncTask.Status.PENDING) {
            sensorAudioAsyncTask.execute();
        }

    }

    public void stop() {
        Log.d(Constants.SENSOR_AUDIO, "stop");

    }

}