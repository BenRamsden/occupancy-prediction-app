package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

public class SensorAudioManager {

    private DataCollectionService mDataCollectionService;

    private SensorAudio sensorAudio;

    private long lastAudioObservation = 0;
    private long audioMinIntervalMillis = 60000;

    public SensorAudioManager(DataCollectionService dataCollectionService) {
        mDataCollectionService = dataCollectionService;
    }

    public void initMicrophone() {
        sensorAudio = new SensorAudio(mDataCollectionService);
    }

    public void startAudio(Location location) {
        if(lastAudioObservation < System.currentTimeMillis() - audioMinIntervalMillis) {

            if(sensorAudio != null) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_AUDIO + " started taking a reading");
                sensorAudio.start(location);
            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_AUDIO + " is null");
            }

            lastAudioObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_AUDIO + " got location update, but minIntevalMillis not passed yet");
        }
    }

}
