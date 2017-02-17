package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

public class SensorHotspotManager {

    private DataCollectionService mDataCollectionService;

    private SensorHotspot sensorHotspot;

    private long lastHotspotObservation = 0;
    private long hotspotMinIntervalMillis = 60000;

    public SensorHotspotManager(DataCollectionService dataCollectionService) {
        mDataCollectionService = dataCollectionService;
        sensorHotspot = new SensorHotspot(mDataCollectionService);
    }

    public void startHotspot(Location location) {
        if(lastHotspotObservation < System.currentTimeMillis() - hotspotMinIntervalMillis) {

            if(sensorHotspot != null) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_HOTSPOT + " started taking a reading");
                sensorHotspot.start(location);
            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_HOTSPOT + " is null");
            }

            lastHotspotObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_HOTSPOT + " got location update, but minIntevalMillis not passed yet");
        }
    }

    public void unregisterReceiver() {
        if(sensorHotspot != null) {
            sensorHotspot.unregisterReceiver();
        }
    }

}
