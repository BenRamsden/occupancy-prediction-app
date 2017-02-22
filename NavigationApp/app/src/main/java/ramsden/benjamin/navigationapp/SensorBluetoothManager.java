package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

public class SensorBluetoothManager {

    private ServiceDataCollection mServiceDataCollection;

    private SensorBluetooth sensorBluetooth;

    private long lastBluetoothObservation = 0;
    private long bluetoothMinIntervalMillis = Constants.DEFAULT_BLUETOOTH_INTERVAL;

    public SensorBluetoothManager(ServiceDataCollection serviceDataCollection) {
        mServiceDataCollection = serviceDataCollection;
        sensorBluetooth = new SensorBluetooth(mServiceDataCollection);
    }

    public void startBluetooth(Location location) {
        if(lastBluetoothObservation < System.currentTimeMillis() - bluetoothMinIntervalMillis) {

            if(sensorBluetooth != null) {
                Log.d(Constants.SENSOR_BLUETOOTH_MANAGER, "Sensor: " + Constants.SENSOR_BLUETOOTH + " started taking a reading");
                sensorBluetooth.start(location);
            } else {
                Log.d(Constants.SENSOR_BLUETOOTH_MANAGER, "Sensor: " + Constants.SENSOR_BLUETOOTH + " is null");
            }

            lastBluetoothObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.SENSOR_BLUETOOTH_MANAGER, "Sensor: " + Constants.SENSOR_BLUETOOTH + " got location update, but minIntevalMillis not passed yet");
        }
    }

    public void unregisterReceiver() {
        if(sensorBluetooth != null) {
            sensorBluetooth.unregisterReceiver();
        }
    }

}
