package ramsden.benjamin.navigationapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

/**
 * Created by ben on 09/02/2017.
 */

public class AccelerometerListener implements SensorEventListener {

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(Constants.ACCELEROMETER_LOG_TAG, "onSensorChanged: " + event);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(Constants.ACCELEROMETER_LOG_TAG, "onAccuracyChanged: " + accuracy);

    }
}
