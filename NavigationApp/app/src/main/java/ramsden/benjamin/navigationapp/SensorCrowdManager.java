package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

public class SensorCrowdManager {

    private DataCollectionService mDataCollectionService;

    private long lastCrowdObservation = 0;
    private long crowdMinIntervalMillis = Constants.DEFAULT_CROWD_INTERVAL;

    public SensorCrowdManager(DataCollectionService dataCollectionService) {
        mDataCollectionService = dataCollectionService;
    }

    public void startCrowd(Location location) {
        if(lastCrowdObservation < System.currentTimeMillis() - crowdMinIntervalMillis) {

            Log.d(Constants.SENSOR_CROWD_MANAGER, "Sensor: " + Constants.SENSOR_CROWD + " should be called, but does not yet exist");

            // TODO: Crowd observations

            lastCrowdObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.SENSOR_CROWD_MANAGER, "Sensor: " + Constants.SENSOR_CROWD + " got location update, but minIntevalMillis not passed yet");
        }
    }

}
