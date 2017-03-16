package ramsden.benjamin.navigationapp;

/**
 * Created by ben on 07/02/2017.
 */

public class Constants {
    public static final long DEFAULT_MIN_GPS_TIME = 15;
    public static final float DEFAULT_MIN_GPS_DISTANCE = 1f;

    public static final long DEFAULT_START_ALL_SENSORS_INTERVAL = DEFAULT_MIN_GPS_TIME * 2 * 1000;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 734;
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 747;
    public static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 757;
    public static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 767;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 777;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 787;
    public static final int MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE = 797;

    public static final String PREFERENCE_FILE_KEY = "ramsden.benjamin.navigationapp.PREF_FILE_1";
    public static final String PREFERENCE_MAP_POLL_INTERVAL = "MAP_POLL_INTERVAL";
    public static final String PREFERENCE_START_ALL_SENSORS_INTERVAL = "DEFAULT_START_ALL_SENSORS_INTERVAL";
    public static final String PREFERENCE_SERVER_URL = "PREFERENCE_SERVER_URL";

    public static final long DEFAULT_MAP_POLL_INTERVAL = 2000;
    public static final String DEFAULT_SERVER_URL = "http://benramsden.me";

    public static final String NAVIGATION_APP = "NavigationApp";
    public static final String DATA_COLLECTION_SERVICE = "ServiceDataCollection";
    public static final String ACTIVITY_CONFIGURE = "ActivityConfigure";
    public static final String GOOGLE_API_CLIENT = "GoogleApiClient";

    public static final String SENSOR_ACCELEROMETER_MANAGER = "SensorAccelerometerMan";
    public static final String SENSOR_AUDIO_MANAGER = "SensorAudioManager";
    public static final String SENSOR_BLUETOOTH_MANAGER = "SensorBluetoothManager";
    public static final String SENSOR_HOTSPOT_MANAGER = "SensorHotspotManager";

    public static final String SENSOR_ACCELEROMETER = "SensorAccelerometer";
    public static final String SENSOR_AUDIO = "SensorAudio";
    public static final String SENSOR_BLUETOOTH = "SensorBluetooth";
    public static final String SENSOR_HOTSPOT = "SensorHotspot";

    public static final String PERMISSIONS = "Permissions";
    public static final String CONTENT_PROVIDER = "ContentProvider";
    public static final String SENT_LOG_ACTIVITY = "ActivitySentLog";

}
