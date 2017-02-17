package ramsden.benjamin.navigationapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

public class SensorAccelerometerManager {

    private DataCollectionService mDataCollectionService;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorAccelerometer sensorAccelerometer;

    private long lastAccelerometerObservation = 0;
    private long accelerometerMinIntervalMillis = 60000;

    public SensorAccelerometerManager(DataCollectionService dataCollectionService) {
        mDataCollectionService = dataCollectionService;
    }

    public void initAccelerometer() {
        mSensorManager = (SensorManager) mDataCollectionService.getSystemService(Context.SENSOR_SERVICE);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorAccelerometer = new SensorAccelerometer(mDataCollectionService);
            mSensorManager.registerListener(sensorAccelerometer, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Accelerometer Sensor SUCCESS (Subscribed)");
        } else {
            mAccelerometer = null;
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Accelerometer Sensor FAILED (NoDefaultSensor)");
        }
    }

    public void startAccelerometer(Location location) {
        if(lastAccelerometerObservation < System.currentTimeMillis() - accelerometerMinIntervalMillis) {

            if(sensorAccelerometer != null) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " started taking a reading");
                sensorAccelerometer.start(location);
            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " is null");
            }

            lastAccelerometerObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " got location update, but minIntevalMillis not passed yet");
        }
    }

}
