package ramsden.benjamin.navigationapp;

import android.content.ContentValues;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorAccelerometer implements SensorEventListener {

    private static final long POLLING_INTERVAL_MILLIS = 5000;

    private boolean enableLogging = true;
    private long last_update_time = 0;
    private long current_index = 0;

    private HashMap<String, JSONObject> acceleration_timeline = new HashMap<>();

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(last_update_time < System.currentTimeMillis() - POLLING_INTERVAL_MILLIS) {
            if(enableLogging) Log.d(Constants.SENSOR_ACCELEROMETER, "onSensorChanged (hit interval): " + event);

            try {
                acceleration_timeline.put(String.valueOf(current_index++),
                        new JSONObject()
                        .put("0", event.values[0])
                        .put("1", event.values[1])
                        .put("2", event.values[2])
                );
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            last_update_time = System.currentTimeMillis();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(enableLogging) Log.d(Constants.SENSOR_ACCELEROMETER, "onAccuracyChanged: " + accuracy);


    }

    public HashMap<String, JSONObject> getAccelerationTimeline() {
        HashMap<String, JSONObject> copy = acceleration_timeline;

        acceleration_timeline = new HashMap<>();
        current_index = 0;

        return copy;
    }
}
