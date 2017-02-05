package ramsden.benjamin.navigationapp;

import android.provider.BaseColumns;

/**
 * Created by ben on 01/02/2017.
 */

/**
 * Sets out the contract for the schema of the Location Database
 * Currently only has one table to store the time and locations recorded from the user
 */

public class NavigationServerContract implements BaseColumns {

    private NavigationServerContract() {}

    public static class Users {
        public static final String TABLE_NAME = "Users";

        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_USERNAME = "username";
        public static final String KEY_PASSWORD = "password";
        public static final String KEY_FULL_NAME = "full_name";
        public static final String KEY_EMAIL_ADDRESS = "email_address";
        public static final String KEY_REGISTER_DATE = "register_date";
        public static final String KEY_API_TOKEN = "api_token";
    }

    public static class Hotspots {
        public static final String TABLE_NAME = "Hotspots";

        public static final String PKEY_ID_HOTSPOT = "idHotspot";

        public static final String KEY_SSID = "ssid";
        public static final String KEY_MAC = "mac";
        public static final String KEY_CHANNEL = "channel";
        public static final String KEY_FREQUENCY = "frequency";
        public static final String KEY_REGISTER_DATE = "register_date";
    }

    public static class HotspotObservations {
        public static final String TABLE_NAME = "HotspotObservations";

        public static final String PKEY_ID_HOTSPOT_OBSERVATION = "idHotspotObservation";
        public static final String PKEY_ID_HOTSPOT = "idHotspot";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_NUMBER_CONNECTED = "number_connected";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    public static class AudioObservations {
        public static final String TABLE_NAME = "AudioObservations";

        public static final String PKEY_ID_AUDIO_OBSERVATION = "idAudioObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_AUDIO_HISTOGRAM = "audio_histogram";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    public static class CrowdObservations {
        public static final String TABLE_NAME = "CrowdObservations";

        public static final String PKEY_ID_CROWD_OBSERVATION = "idCrowdObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_OCCUPANCY_ESTIMATE = "occupancy_estimate";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    public static class BluetoothObservations {
        public static final String TABLE_NAME = "BluetoothObservations";

        public static final String PKEY_ID_BLUETOOTH_OBSERVATION = "idBluetoothObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_BLUETOOTH_COUNT = "bluetooth_count";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    public static class AccelerometerObservations {
        public static final String TABLE_NAME = "AccelerometerObservations";

        public static final String PKEY_ID_ACCELEROMETER_OBSERVATION = "idAccelerometerObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_ACCELERATION_TIMELINE = "acceleration_timeline";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }
}