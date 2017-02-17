package ramsden.benjamin.navigationapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;

/**
 * Manages the applications LocationListener
 * Ensures that the location listener continues to provide the content provider with information
 * Can be made into a foreground notification at the users request, to stop it being killed after the app closes
 * It is assumed that when this is called the Location permission has been granted
 */

public class DataCollectionService extends Service {

    private static final int pendingIntentCode = 333;
    private static final int foregroudCode = 343;
    private Notification foregroundNotif;
    private boolean notifActive = false;

    private MyLocationListener locationListener;

    /*************** Accelerometer */

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorAccelerometer sensorAccelerometer;

    private long lastAccelerometerObservation = 0;
    private long accelerometerMinIntervalMillis = 60000;

    private void initAccelerometer() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorAccelerometer = new SensorAccelerometer(DataCollectionService.this);
            mSensorManager.registerListener(sensorAccelerometer, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Accelerometer Sensor SUCCESS (Subscribed)");
        } else {
            mAccelerometer = null;
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Accelerometer Sensor FAILED (NoDefaultSensor)");
        }
    }

    public void startAccelerometer(Location location) {
        if(lastAccelerometerObservation < System.currentTimeMillis() - accelerometerMinIntervalMillis) {

            if(sensorAccelerometer != null) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " started taking a reading");
                sensorAccelerometer.start(location);
            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " is null");
            }

            lastAccelerometerObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " got location update, but minIntevalMillis not passed yet");
        }
    }

    /*******************************/

    /*************** Microphone */

    private SensorAudio sensorAudio;

    private long lastAudioObservation = 0;
    private long audioMinIntervalMillis = 60000;

    private void initMicrophone() {
        sensorAudio = new SensorAudio(this);
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

    /*******************************/

    /*************** Bluetooth */

    private SensorBluetooth sensorBluetooth;

    private long lastBluetoothObservation = 0;
    private long bluetoothMinIntervalMillis = 60000;

    private void initBluetooth() {
        sensorBluetooth = new SensorBluetooth(this);
    }

    public void startBluetooth(Location location) {
        if(lastBluetoothObservation < System.currentTimeMillis() - bluetoothMinIntervalMillis) {

            if(sensorBluetooth != null) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_BLUETOOTH + " started taking a reading");
                sensorBluetooth.start(location);
            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_BLUETOOTH + " is null");
            }

            lastBluetoothObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_BLUETOOTH + " got location update, but minIntevalMillis not passed yet");
        }
    }

    /*******************************/

    /*************** Wifi */

    private SensorHotspot sensorHotspot;

    private long lastHotspotObservation = 0;
    private long hotspotMinIntervalMillis = 60000;

    private void initWifi() {
        sensorHotspot = new SensorHotspot(this);
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

    /*******************************/

    /*************** Crowd */

    private long lastCrowdObservation = 0;
    private long crowdMinIntervalMillis = 60000;

    public void startCrowd(Location location) {
        if(lastCrowdObservation < System.currentTimeMillis() - crowdMinIntervalMillis) {

            // TODO: Crowd observations

            lastCrowdObservation = System.currentTimeMillis();
        } else {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_CROWD + " got location update, but minIntevalMillis not passed yet");
        }
    }

    /*******************************/

    /* Provides the activities using the service the ability to
     * Retreive and Change the paramaters given to the location listener
     */
    private long minTime = Constants.DEFAULT_MIN_TIME;  //minimum time different between 2 location updates
    public long getMinTime() { return minTime; }

    private float minDistance = Constants.DEFAULT_MIN_DISTANCE;   //minimum distance between 2 location updates
    public float getMinDistance() { return minDistance; }

    public void updateLocationListenerOptions(long minTime, float minDistance) {
        this.minTime = minTime;
        this.minDistance = minDistance;
        initLocationListener();
    }

    private final IBinder myBinder = new MyBinder();

    public class MyBinder extends Binder {
        DataCollectionService getService() {
            return DataCollectionService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Constants.DATA_COLLECTION_SERVICE,"DataCollectionService onBind");
        return myBinder;
    }

    /* Sets up the notification, and saves it in a variable ready to be used
     * Initializes the location listener */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.DATA_COLLECTION_SERVICE, "DataCollectionService onCreate");

        /* Create notification ready to create foreground Service notification on event user plays music */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setColor(Color.WHITE)
                .setSmallIcon(R.drawable.ic_runner_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_runner_icon))
                .setContentText("NavigationApp");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, pendingIntentCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        foregroundNotif = builder.build();

        initAccelerometer();

        initMicrophone();

        initBluetooth();

        initWifi();

        initLocationListener();

        /* TODO: Experiment with calling locationListener.locationChanged(location)
         * With the last received location, if no update has been provided by system for a period
         * Meaning we get up to date data at the current location */

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
        Log.d(Constants.DATA_COLLECTION_SERVICE, "DataCollectionService startForegroundNotification");
        if(!notifActive) {
            startForeground(foregroudCode, foregroundNotif);
            notifActive = true;
        }
    }

    /* removes the foreground notificaiton if it does exist  */
    public void stopForegroundNotification() {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "DataCollectionService stopForegroundNotification");
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
            Toast.makeText(this, "Err: DataCollectionService instantiated, without ACCESS_FINE_LOCATION Permission",Toast.LENGTH_LONG).show();
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
            locationListener = new MyLocationListener(this);
        }

        try {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "initLocationListener requested location updates with minTime: " + minTime + " minDistance: " + minDistance);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
        } catch(SecurityException e) {
            Log.d(Constants.DATA_COLLECTION_SERVICE, e.toString());
        }
    }

    /* Makes sure that the location listener is not still receiving updates
     * If the location service has been destroyed */
    @Override
    public void onDestroy() {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "DataCollectionService onDestroy");

        if(locationListener != null) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "onDestroy (DataCollectionService) removed updates from locationListener");
                locationManager.removeUpdates(locationListener);
            } catch(SecurityException e) {
                Log.d(Constants.DATA_COLLECTION_SERVICE, e.toString());
            }
        }

        if(sensorBluetooth != null) {
            sensorBluetooth.unregisterReceiver();
        }

        if(sensorHotspot != null) {
            sensorHotspot.unregisterReceiver();
        }

        super.onDestroy();
    }

}
