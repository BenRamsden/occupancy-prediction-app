package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

public class SensorBluetoothManager {

    private ServiceDataCollection mServiceDataCollection;

    private SensorBluetooth sensorBluetooth;

    public SensorBluetoothManager(ServiceDataCollection serviceDataCollection) {
        mServiceDataCollection = serviceDataCollection;
        sensorBluetooth = new SensorBluetooth(mServiceDataCollection);
    }

    public void startBluetooth(Location location) {
        if(sensorBluetooth != null) {
            Log.d(Constants.SENSOR_BLUETOOTH_MANAGER, "Sensor: " + Constants.SENSOR_BLUETOOTH + " started taking a reading");
            sensorBluetooth.start(location);
        } else {
            Log.d(Constants.SENSOR_BLUETOOTH_MANAGER, "Sensor: " + Constants.SENSOR_BLUETOOTH + " is null");
        }
    }

    public void unregisterReceiver() {
        if(sensorBluetooth != null) {
            sensorBluetooth.unregisterReceiver();
        }
    }

}
