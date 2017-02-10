package ramsden.benjamin.navigationapp;

import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorMicrophone {
    private MediaRecorder mRecorder = null;

    public void start() {
        Log.d(Constants.SENSOR_MICROPHONE, "start");

        try {
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");
                mRecorder.prepare();
                mRecorder.start();
            }
        } catch(IOException ex) {
            Log.d(Constants.SENSOR_MICROPHONE, "IOException: " + ex);
            mRecorder = null;
        }

    }

    public void stop() {
        Log.d(Constants.SENSOR_MICROPHONE, "stop");

        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            } catch (RuntimeException ex) {
                Log.d(Constants.SENSOR_MICROPHONE, "Failed to stop mRecorder with ex: " + ex);
            }
        }
    }

    @Nullable
    public Integer getAmplitude() {
        Log.d(Constants.SENSOR_MICROPHONE, "getAmplitude");

        if (mRecorder != null) {
            return  mRecorder.getMaxAmplitude();
        }

        return null;

    }
}
