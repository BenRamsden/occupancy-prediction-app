package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

public class SensorAudioManager {

    private ServiceDataCollection mServiceDataCollection;

    private SensorAudio sensorAudio;

    public SensorAudioManager(ServiceDataCollection serviceDataCollection) {
        mServiceDataCollection = serviceDataCollection;
        sensorAudio = new SensorAudio(mServiceDataCollection);
    }

    public void startAudio(Location location) {
        if(sensorAudio != null) {
            Log.d(Constants.SENSOR_AUDIO_MANAGER, "Sensor: " + Constants.SENSOR_AUDIO + " started taking a reading");
            sensorAudio.start(location);
        } else {
            Log.d(Constants.SENSOR_AUDIO_MANAGER, "Sensor: " + Constants.SENSOR_AUDIO + " is null");
        }
    }

}
