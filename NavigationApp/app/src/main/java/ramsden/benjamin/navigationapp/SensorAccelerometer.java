package ramsden.benjamin.navigationapp;

import android.content.ContentValues;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by ben on 09/02/2017.
 */

public class SensorAccelerometer implements SensorEventListener {

    private SensorAccelerometerAsyncTask sensorAccelerometerAsyncTask;

    private static final long POLLING_INTERVAL_MILLIS = 5000;

    private long last_update_time = 0;
    private long current_index = 0;

    private Context mContext;
    private Location mLocation;

    private JSONObject acceleration_timeline = new JSONObject();

    public SensorAccelerometer(Context context) {
        mContext = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(last_update_time < System.currentTimeMillis() - POLLING_INTERVAL_MILLIS) {
            //Log.d(Constants.SENSOR_ACCELEROMETER, "onSensorChanged (hit interval): " + event);

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
        Log.d(Constants.SENSOR_ACCELEROMETER, "onAccuracyChanged: " + accuracy);
    }

    /* TODO: Test for concurrency issues over acceleration_timeline (most likely none due to SensorEventListener being on same thread */
    public JSONObject copyAccelerationTimeline() {
        JSONObject copy = acceleration_timeline;

        acceleration_timeline = new JSONObject();
        current_index = 0;

        return copy;
    }

    public void start(Location location) {
        Log.d(Constants.SENSOR_ACCELEROMETER, "start");

        if(sensorAccelerometerAsyncTask != null && sensorAccelerometerAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(Constants.SENSOR_BLUETOOTH, "sensorAccelerometerAsyncTask has status RUNNING, refused to start another");
            return;
        }

        mLocation = location;

        sensorAccelerometerAsyncTask = new SensorAccelerometerAsyncTask();

        sensorAccelerometerAsyncTask.execute();
    }

    class SensorAccelerometerAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

            JSONObject acceleration_timeline_copy = copyAccelerationTimeline();

            if(acceleration_timeline_copy.length() == 0) {
                Log.d(Constants.SENSOR_ACCELEROMETER, "Not sending, acceleration_timeline has length 0");
            } else {
                ContentValues accelerometerValues = new ContentValues();
                accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_LATITUDE, mLocation.getLatitude());
                accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_LONGITUDE, mLocation.getLongitude());
                accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_ACCELERATION_TIMELINE, acceleration_timeline_copy.toString());
                accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_OBSERVATION_DATE, current_date);
                Uri accelerometerUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.AccelerometerObservations.TABLE_NAME);
                mContext.getContentResolver().insert(accelerometerUri, accelerometerValues);

                Log.d(Constants.SENSOR_ACCELEROMETER, "Sent acceleration timeline: " + acceleration_timeline_copy.toString());
            }

            return null;
        }
    }
}
