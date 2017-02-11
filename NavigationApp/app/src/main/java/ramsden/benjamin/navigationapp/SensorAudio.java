package ramsden.benjamin.navigationapp;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorAudio {
    private MediaRecorder mRecorder = null;
    private String mFileName = null;

    public SensorAudio(Context context) {
        // Record to the external cache directory for visibility
        mFileName = context.getExternalCacheDir().getAbsolutePath();
        mFileName += "/sensoraudiotest.3gp";
    }

    public void start() {
        Log.d(Constants.SENSOR_MICROPHONE, "startScan");

        try {
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile(mFileName);
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
