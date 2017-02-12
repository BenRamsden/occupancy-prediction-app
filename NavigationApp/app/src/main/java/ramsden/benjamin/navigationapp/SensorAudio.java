package ramsden.benjamin.navigationapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorAudio {

    public SensorAudio(Context context) {
        // Record to the external cache directory for visibility

    }

    public void start() {
        Log.d(Constants.SENSOR_MICROPHONE, "startScan");

    }

    public void stop() {
        Log.d(Constants.SENSOR_MICROPHONE, "stop");

    }

    @Nullable
    public Integer getAmplitude() {
        Log.d(Constants.SENSOR_MICROPHONE, "getAmplitude");

        return 0;

    }
}