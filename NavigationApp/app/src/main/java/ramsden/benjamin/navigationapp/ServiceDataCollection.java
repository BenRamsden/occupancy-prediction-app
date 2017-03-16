package ramsden.benjamin.navigationapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages the applications LocationListener
 * Ensures that the location listener continues to provide the content provider with information
 * Can be made into a foreground notification at the users request, to stop it being killed after the app closes
 * It is assumed that when this is called the Location permission has been granted
 */

public class ServiceDataCollection extends Service {

    private static final int pendingIntentCode = 333;
    private static final int foregroudCode = 343;
    private Notification foregroundNotif;
    private boolean notifActive = false;

    private SensorAccelerometerManager sensorAccelerometerManager;
    private SensorAudioManager sensorAudioManager;
    private SensorBluetoothManager sensorBluetoothManager;
    private SensorHotspotManager sensorHotspotManager;

    private MyLocationListener locationListener;

    private Location lastLocation = null;

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(Constants.MY_LOCATION_LISTENER,"onLocationChanged: " + location.toString());
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { Log.d(Constants.MY_LOCATION_LISTENER,"onStatusChanged: " + status); }

        @Override
        public void onProviderEnabled(String provider) { Log.d(Constants.MY_LOCATION_LISTENER,"onProviderEnabled: " + provider); }

        @Override
        public void onProviderDisabled(String provider) { Log.d(Constants.MY_LOCATION_LISTENER,"onProviderDisabled: " + provider); }
    }

    /* Provides the activities using the service the ability to
     * Retreive and Change the paramaters given to the location listener
     */
    private long minTime = Constants.DEFAULT_MIN_GPS_TIME;  //minimum time different between 2 location updates
    public long getMinTime() { return minTime; }

    private float minDistance = Constants.DEFAULT_MIN_GPS_DISTANCE;   //minimum distance between 2 location updates
    public float getMinDistance() { return minDistance; }

    public Location getLastLocation() { return lastLocation; };

    private final IBinder myBinder = new MyBinder();

    public class MyBinder extends Binder {
        ServiceDataCollection getService() {
            return ServiceDataCollection.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Constants.DATA_COLLECTION_SERVICE,"ServiceDataCollection onBind");
        return myBinder;
    }

    private SharedPreferences sharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch(key) {
                case Constants.PREFERENCE_MIN_DISTANCE:
                    minDistance = sharedPreferences.getFloat(Constants.PREFERENCE_MIN_DISTANCE, Constants.DEFAULT_MIN_GPS_DISTANCE);
                    initLocationListener();
                    Toast.makeText(getBaseContext(), "ServiceDataCollection onSharedPreferenceChanged minDistance:" + minDistance, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.PREFERENCE_MIN_TIME:
                    minTime = sharedPreferences.getLong(Constants.PREFERENCE_MIN_TIME, Constants.DEFAULT_MIN_GPS_TIME);
                    initLocationListener();
                    Toast.makeText(getBaseContext(), "ServiceDataCollection onSharedPreferenceChanged minTime:" + minTime, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL:
                    Long start_all_sensors_interval = sharedPreferences.getLong(Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL, Constants.DEFAULT_START_ALL_SENSORS_INTERVAL);
                    initSensorTimer(start_all_sensors_interval);
                    Toast.makeText(getBaseContext(), "ServiceDataCollection onSharedPreferenceChanged start_all_sensors_interval: " + start_all_sensors_interval, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    /* Sets up the notification, and saves it in a variable ready to be used
     * Initializes the location listener */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.DATA_COLLECTION_SERVICE, "ServiceDataCollection onCreate");

        sharedPreferences = getBaseContext().getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        minDistance = sharedPreferences.getFloat(Constants.PREFERENCE_MIN_DISTANCE, Constants.DEFAULT_MIN_GPS_DISTANCE);
        minTime = sharedPreferences.getLong(Constants.PREFERENCE_MIN_TIME, Constants.DEFAULT_MIN_GPS_TIME);

        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        /* Create notification ready to create foreground Service notification on event user plays music */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setColor(Color.WHITE)
                .setSmallIcon(R.drawable.ic_runner_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_runner_icon))
                .setContentText("NavigationApp");

        /* TODO: Fire generic request for launcher instead?? SEE AndroidManifest intent filter */
        Intent resultIntent = new Intent(this, ActivityNavigation.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, pendingIntentCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        foregroundNotif = builder.build();

        sensorAccelerometerManager = new SensorAccelerometerManager(this);

        sensorAudioManager = new SensorAudioManager(this);

        sensorBluetoothManager = new SensorBluetoothManager(this);

        sensorHotspotManager = new SensorHotspotManager(this);

        initLocationListener();

        /* TODO: Experiment with calling locationListener.locationChanged(location)
         * With the last received location, if no update has been provided by system for a period
         * Meaning we get up to date data at the current location */

        Long start_all_sensors_interval = sharedPreferences.getLong(Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL, Constants.DEFAULT_START_ALL_SENSORS_INTERVAL);

        initSensorTimer(start_all_sensors_interval);

    }

    private Timer sensor_timer;

    private void initSensorTimer(long start_all_sensors_interval) {
        if(sensor_timer != null) {
            sensor_timer.cancel();
            sensor_timer = null;
        }

        sensor_timer = new Timer();

        sensor_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(lastLocation != null) {
                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Started all sensors with lastLocation");
                    startAllSensors(lastLocation);
                } else {
                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Could not start all sensors, lastLocation is null");
                }
            }
        }, start_all_sensors_interval, start_all_sensors_interval);
    }

    private void startAllSensors(Location location) {

        if(sensorAccelerometerManager != null) {
            sensorAccelerometerManager.startAccelerometer(location);
        }

        if(sensorAudioManager != null) {
            sensorAudioManager.startAudio(location);
        }

        if(sensorBluetoothManager != null) {
            sensorBluetoothManager.startBluetooth(location);
        }

        if(sensorHotspotManager != null) {
            sensorHotspotManager.startHotspot(location);
        }

    }

    public boolean sendCrowdObservation(Integer user_estimate) {
        if(lastLocation == null) {
            return false;
        }

        String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

        Uri uri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.CrowdObservations.TABLE_NAME);
        ContentValues contentValues = new ContentValues();
        contentValues.put(NavigationContract.CrowdObservations.KEY_LATITUDE, lastLocation.getLatitude());
        contentValues.put(NavigationContract.CrowdObservations.KEY_LONGITUDE, lastLocation.getLongitude());
        contentValues.put(NavigationContract.CrowdObservations.KEY_OCCUPANCY_ESTIMATE, user_estimate);
        contentValues.put(NavigationContract.CrowdObservations.KEY_OBSERVATION_DATE, current_date);
        getContentResolver().insert(uri, contentValues);

        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service will be killed by the application on exit
        // If the user has not set it to a foreground service
        // START_STICKY will be used in case the service is killed by something other than the application
        return START_STICKY;
    }

    public boolean isForegroundNotification() {
        return notifActive;
    }

    /* creates the foreground notification to keep the service alive
     * ensures the notification is not already active */
    public void startForegroundNotification() {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "ServiceDataCollection startForegroundNotification");
        if(!notifActive) {
            startForeground(foregroudCode, foregroundNotif);
            notifActive = true;
        }
    }

    /* removes the foreground notificaiton if it does exist  */
    public void stopForegroundNotification() {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "ServiceDataCollection stopForegroundNotification");
        if(notifActive) {
            stopForeground(true);
            notifActive = false;
        }
    }

    /* Initializes the locationListener with the paramaters stored in the service
     * Can be called multiple times, if the locationListener has already been
     * initialized and is receiving updates, this method will request the location
     * manager remove updates the listener, and re-request the updates with the
     * Updated parameters */
    private void initLocationListener() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Err: ServiceDataCollection instantiated, without ACCESS_FINE_LOCATION Permission",Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(Constants.DATA_COLLECTION_SERVICE, "initLocationListener called");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationListener != null) {
            /* Remove any previous location listener request */
            try {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "initLocationListener removed updates from locationListener");
                locationManager.removeUpdates(locationListener);
            } catch(SecurityException e) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, e.toString());
            }
        } else {
            /* Create instance of the location listener for the first time */
            locationListener = new MyLocationListener();
        }

        try {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "initLocationListener requested location updates with minTime: " + minTime + " minDistance: " + minDistance);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);

        } catch(SecurityException e) {
            Log.d(Constants.DATA_COLLECTION_SERVICE, e.toString());
        }
    }

    /* Makes sure that the location listener is not still receiving updates
     * If the location service has been destroyed */
    @Override
    public void onDestroy() {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "ServiceDataCollection onDestroy");

        if(locationListener != null) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "onDestroy (ServiceDataCollection) removed updates from locationListener");
                locationManager.removeUpdates(locationListener);
            } catch(SecurityException e) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, e.toString());
            }
        }

        if(sensor_timer != null) {
            sensor_timer.cancel();
        }

        if(sensorBluetoothManager != null) {
            sensorBluetoothManager.unregisterReceiver();
        }

        if(sensorHotspotManager != null) {
            sensorHotspotManager.unregisterReceiver();
        }

        super.onDestroy();
    }

}
