package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.util.Log;

/**
 * This class manages and synchronizes an instance of SensorHotspot for the Service Data Collection
 */

public class SensorHotspotManager {

    private ServiceDataCollection mServiceDataCollection;

    private SensorHotspot sensorHotspot;

    public SensorHotspotManager(ServiceDataCollection serviceDataCollection) {
        mServiceDataCollection = serviceDataCollection;
        sensorHotspot = new SensorHotspot(mServiceDataCollection);
    }

    public void startHotspot(Location location) {
        if(sensorHotspot != null) {
            Log.d(Constants.SENSOR_HOTSPOT_MANAGER, "Sensor: " + Constants.SENSOR_HOTSPOT + " started taking a reading");
            sensorHotspot.start(location);
        } else {
            Log.d(Constants.SENSOR_HOTSPOT_MANAGER, "Sensor: " + Constants.SENSOR_HOTSPOT + " is null");
        }
    }

    public void unregisterReceiver() {
        if(sensorHotspot != null) {
            sensorHotspot.unregisterReceiver();
        }
    }

}
