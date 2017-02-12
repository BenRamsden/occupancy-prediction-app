package ramsden.benjamin.navigationapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.media.RemotePlaybackClient;
import android.util.Log;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorAudio {

    RecordAudio recordAudio;

    public SensorAudio(Context context) {
        // Record to the external cache directory for visibility
        recordAudio = new RecordAudio();

    }

    public void start() {
        Log.d(Constants.SENSOR_AUDIO, "startScan");

        if(recordAudio.getStatus() == AsyncTask.Status.PENDING) {
            recordAudio.execute();
        }

    }

    public void stop() {
        Log.d(Constants.SENSOR_AUDIO, "stop");

    }

}