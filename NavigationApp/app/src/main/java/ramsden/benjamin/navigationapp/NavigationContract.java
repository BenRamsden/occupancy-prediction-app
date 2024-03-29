package ramsden.benjamin.navigationapp;

import android.provider.BaseColumns;

/**
 * Created by ben on 01/02/2017.
 */

/**
 * Sets out the contract for the schema of the Location Database
 * Currently only has one table to store the time and locations recorded from the user
 */

public class NavigationContract implements BaseColumns {

    private NavigationContract() {}

    /**
     * The field and table names to be used when sending user_data to the webserver
     */
    public static class Users {
        public static final String TABLE_NAME = "users";

        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_USERNAME = "username";
        public static final String KEY_PASSWORD = "password";
        public static final String KEY_FULL_NAME = "full_name";
        public static final String KEY_EMAIL_ADDRESS = "email_address";
        public static final String KEY_REGISTER_DATE = "register_date";
        public static final String KEY_API_TOKEN = "api_token";
    }

    /**
     * The field and table names to be used when sending hotspots to the webserver
     */
    public static class Hotspots {
        public static final String TABLE_NAME = "hotspots";

        public static final String PKEY_ID_HOTSPOT = "idHotspot";

        public static final String KEY_SSID = "ssid";
        public static final String KEY_MAC = "mac";
        public static final String KEY_FREQUENCY = "frequency";
    }

    /**
     * The field and table names to be used when sending hotspot observations to the webserver
     */
    public static class HotspotObservations {
        public static final String TABLE_NAME = "hotspot_observations";

        public static final String PKEY_ID_HOTSPOT_OBSERVATION = "idHotspotObservation";
        public static final String PKEY_ID_HOTSPOT = "idHotspot";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_SIGNAL_LEVEL = "signal_level";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    /**
     * The field and table names to be used when sending audio observations to the webserver
     */
    public static class AudioObservations {
        public static final String TABLE_NAME = "audio_observations";

        public static final String PKEY_ID_AUDIO_OBSERVATION = "idAudioObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_AUDIO_HISTOGRAM = "audio_histogram";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    /**
     * The field and table names to be used when sending crowd observations to the webserver
     */
    public static class CrowdObservations {
        public static final String TABLE_NAME = "crowd_observations";

        public static final String PKEY_ID_CROWD_OBSERVATION = "idCrowdObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_OCCUPANCY_ESTIMATE = "occupancy_estimate";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    /**
     * The field and table names to be used when sending bluetooth observations to the webserver
     */
    public static class BluetoothObservations {
        public static final String TABLE_NAME = "bluetooth_observations";

        public static final String PKEY_ID_BLUETOOTH_OBSERVATION = "idBluetoothObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_BLUETOOTH_COUNT = "bluetooth_count";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    /**
     * The field and table names to be used when sending accelerometer observations to the webserver
     */
    public static class AccelerometerObservations {
        public static final String TABLE_NAME = "accelerometer_observations";

        public static final String PKEY_ID_ACCELEROMETER_OBSERVATION = "idAccelerometerObservation";
        public static final String PKEY_ID_USER = "idUser";

        public static final String KEY_LATITUDE = "lat";
        public static final String KEY_LONGITUDE = "lng";

        public static final String KEY_ACCELERATION_TIMELINE = "acceleration_timeline";
        public static final String KEY_OBSERVATION_DATE = "observation_date";
    }

    /**
     * The fields to be used when requesting occupancy estimates from webserver
     */
    public static class OccupancyEstimate {
        public static final String ARG_LAT = "lat";
        public static final String ARG_LNG = "lng";
        public static final String ARG_START_DATE = "start_date";
        public static final String ARG_END_DATE = "end_date";

        public static final String ARG_LIVE_ENABLED = "live_enabled";
        public static final String ARG_TIME_ENABLED = "time_enabled";
        public static final String ARG_CROWD_ENABLED = "crowd_enabled";

        public static final String RESPONSE_OCCUPANCY = "occupancy";

        public static final String EXTRA_OCCUPANCY_ESTIMATE = "occupancy_estimate";
        public static final String EXTRA_MODE = "mode";
    }

    /**
     * The additional field names required when requesting BULK occupancy estimates from webserver
     */
    public static class OccupancyEstimateBulk {
        public static final String ARG_LAT_LNG_LIST = "lat_lng_list";

        public static final String RESPONSE_LAT_LNG_OCCUPANCY_LIST = "lat_lng_occupancy_list";

        public static final String EXTRA_LAT_LNG_OCCUPANCY_LIST = "lat_lng_occupancy_list";
        public static final String EXTRA_MODE = "mode";

    }

    /**
     * Used between the Content Provider and the Broadcast Receiver in ActivityNavigation
     * to pass error messages to the UI
     */
    public static class ErrorUI {
        public static final String EXTRA_MODE = "mode";
        public static final String ERROR_UI_MODE = "error_ui_mode";
        public static final String ERROR_MESSAGE = "errMessage";
    }
}