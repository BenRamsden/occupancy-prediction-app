package ramsden.benjamin.navigationapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

/**
 * This class manages and synchronizes an instance of SensorAccelerometer for the Service Data Collection
 */

public class SensorAccelerometerManager {

    private ServiceDataCollection mServiceDataCollection;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorAccelerometer sensorAccelerometer;

    public SensorAccelerometerManager(ServiceDataCollection serviceDataCollection) {
        mServiceDataCollection = serviceDataCollection;

        mSensorManager = (SensorManager) mServiceDataCollection.getSystemService(Context.SENSOR_SERVICE);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorAccelerometer = new SensorAccelerometer(mServiceDataCollection);
            mSensorManager.registerListener(sensorAccelerometer, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(Constants.SENSOR_ACCELEROMETER_MANAGER, "Accelerometer Sensor SUCCESS (Subscribed)");
        } else {
            mAccelerometer = null;
            Log.d(Constants.SENSOR_ACCELEROMETER_MANAGER, "Accelerometer Sensor FAILED (NoDefaultSensor)");
        }
    }

    public void startAccelerometer(Location location) {
        if(sensorAccelerometer != null) {
            Log.d(Constants.SENSOR_ACCELEROMETER_MANAGER, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " started taking a reading");
            sensorAccelerometer.start(location);
        } else {
            Log.d(Constants.SENSOR_ACCELEROMETER_MANAGER, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " is null");
        }
    }

}
