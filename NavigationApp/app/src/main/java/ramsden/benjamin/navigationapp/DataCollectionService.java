package ramsden.benjamin.navigationapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
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
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    private MyLocationListener locationListener = null;

    class MyLocationListener implements LocationListener {

        long lastAccelerometerObservation = 0;
        long accelerometerMinIntervalMillis = 60000;

        long lastAudioObservation = 0;
        long audioMinIntervalMillis = 60000;

        long lastBluetoothObservation = 0;
        long bluetoothMinIntervalMillis = 60000;

        long lastCrowdObservation = 0;
        long crowdMinIntervalMillis = 60000;

        long lastHotspotObservation = 0;
        long hotspotMinIntervalMillis = 60000;

        @Override
        public void onLocationChanged(Location location) {
            Log.d(Constants.DATA_COLLECTION_SERVICE,"onLocationChanged: " + location.toString());

            String current_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // collect Accelerometer Observations
            if(lastAccelerometerObservation < System.currentTimeMillis() - accelerometerMinIntervalMillis) {
                AsyncTask sendAccelerometer = new SendAccelerometer();
                sendAccelerometer.execute(location);
                lastAccelerometerObservation = System.currentTimeMillis();
            }

            // collect Audio Observations
            if(lastAudioObservation < System.currentTimeMillis() - audioMinIntervalMillis) {
                AsyncTask sendAudio = new SendAudio();
                sendAudio.execute(location);
                lastAudioObservation = System.currentTimeMillis();
            }

            // collect Bluetooth Observations
            if(lastBluetoothObservation < System.currentTimeMillis() - bluetoothMinIntervalMillis) {
                AsyncTask sendBluetooth = new SendBluetooth();
                sendBluetooth.execute(location);
                lastBluetoothObservation = System.currentTimeMillis();
            }

            // collect Crowd Observations
            if(lastCrowdObservation < System.currentTimeMillis() - crowdMinIntervalMillis) {
                // TODO: Crowd observations
                lastCrowdObservation = System.currentTimeMillis();
            }

            // collect Hotspot Observations
            if(lastHotspotObservation < System.currentTimeMillis() - hotspotMinIntervalMillis) {
                AsyncTask sendHotspot = new SendWifi();
                sendHotspot.execute(location);
                lastHotspotObservation = System.currentTimeMillis();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(Constants.DATA_COLLECTION_SERVICE,"onStatusChanged: " + status);

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(Constants.DATA_COLLECTION_SERVICE,"onProviderEnabled: " + provider);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(Constants.DATA_COLLECTION_SERVICE,"onProviderDisabled: " + provider);

        }
    }

    /*************** Accelerometer */

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorAccelerometer mAccelerometerListener;

    private void initAccelerometer() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mAccelerometerListener = new SensorAccelerometer();
            mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Accelerometer Sensor SUCCESS (Subscribed)");
        } else {
            mAccelerometer = null;
            Log.d(Constants.DATA_COLLECTION_SERVICE, "Accelerometer Sensor FAILED (NoDefaultSensor)");
        }
    }

    /*******************************/

    /*************** Microphone */

    SensorAudio mMicrophone;

    private void initMicrophone() {
        mMicrophone = new SensorAudio(this);
    }

    /*******************************/

    /*************** Bluetooth */

    SensorBluetooth mBluetooth;

    private void initBluetooth() {
        mBluetooth = new SensorBluetooth(this);
    }

    /*******************************/

    /*************** Wifi */

    SensorHotspot mWifi;

    private void initWifi() {
        mWifi = new SensorHotspot(this);
    }

    /*******************************/

    /* Provides the activities using the service the ability to
     * Retreive and Change the paramaters given to the location listener
     */
    private long minTime = MainActivity.DEFAULT_MIN_TIME;  //minimum time different between 2 location updates
    public long getMinTime() { return minTime; }

    private float minDistance = MainActivity.DEFAULT_MIN_DISTANCE;   //minimum distance between 2 location updates
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
            locationListener = new MyLocationListener();
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

        super.onDestroy();
    }


    class SendAccelerometer extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            Location location = (Location) params[0];

            String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

            if(mAccelerometerListener != null) {
                HashMap<String, JSONObject> acceleration_timeline = mAccelerometerListener.getAccelerationTimeline();

                JSONObject acceleration_timeline_json = new JSONObject(acceleration_timeline);

                if(acceleration_timeline_json.length() == 0) {
                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Result: acceleration_timeline has length 0");
                } else {
                    ContentValues accelerometerValues = new ContentValues();
                    accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_LATITUDE, location.getLatitude());
                    accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_LONGITUDE, location.getLongitude());
                    accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_ACCELERATION_TIMELINE, acceleration_timeline_json.toString());
                    accelerometerValues.put(NavigationContract.AccelerometerObservations.KEY_OBSERVATION_DATE, current_date);
                    Uri accelerometerUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.AccelerometerObservations.TABLE_NAME);
                    getContentResolver().insert(accelerometerUri, accelerometerValues);

                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Sent acceleration timeline: " + acceleration_timeline_json.toString());
                }

            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_ACCELEROMETER + " is null");
            }

            return null;
        }
    }

    class SendBluetooth extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            Location location = (Location) params[0];

            String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

            if(mBluetooth != null) {
                mBluetooth.startDiscovery();

                int max_bluetooth_polls = 300;
                int bluetooth_polls = 0;

                Integer bluetooth_count = 0;
                int consistent_count = 0;

                while(consistent_count < 100 && bluetooth_polls < max_bluetooth_polls) {
                    Integer temp_bluetooth_count = mBluetooth.getBluetoothDeviceCount();

                    if(temp_bluetooth_count == bluetooth_count) {
                        consistent_count++;
                    } else {
                        consistent_count = 0;
                        bluetooth_count = temp_bluetooth_count;
                    }

                    Log.d(Constants.SENSOR_BLUETOOTH, "bluetooth_count: " + bluetooth_count + " consistent_count: " + consistent_count + " bluetooth_polls: " + bluetooth_polls);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    bluetooth_polls++;
                }


                if(bluetooth_count == null) {
                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Result: bluetooth_count is null");
                } else {
                    ContentValues bluetoothValues = new ContentValues();
                    bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_LATITUDE, location.getLatitude());
                    bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_LONGITUDE, location.getLongitude());
                    bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_BLUETOOTH_COUNT, bluetooth_count);
                    bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_OBSERVATION_DATE, current_date);
                    Uri bluetoothUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.BluetoothObservations.TABLE_NAME);
                    getContentResolver().insert(bluetoothUri, bluetoothValues);

                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Sent bluetooth_count: " + bluetooth_count);
                }

            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_BLUETOOTH + " is null");
            }

            return null;
        }
    }

    class SendAudio extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            Location location = (Location) params[0];

            String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

            if(mMicrophone != null) {
                mMicrophone.start();

                int max_audio_polls = 100;
                int audio_polls = 0;

                Integer amplitude = 0;

                while(audio_polls < max_audio_polls) {
                    amplitude = mMicrophone.getAmplitude();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    audio_polls++;
                }

                mMicrophone.stop();

                if(amplitude == null) {
                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Result: audio_histogram is null");

                } else {
                    JSONObject audio_histogram = new JSONObject();
                    JSONObject entry0 = new JSONObject();

                    try {
                        entry0.put("lo",0).put("hi",20000).put("vl", amplitude);
                        audio_histogram.put("0", entry0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ContentValues audioValues = new ContentValues();
                    audioValues.put(NavigationContract.AudioObservations.KEY_LATITUDE, location.getLatitude());
                    audioValues.put(NavigationContract.AudioObservations.KEY_LONGITUDE, location.getLongitude());
                    audioValues.put(NavigationContract.AudioObservations.KEY_AUDIO_HISTOGRAM, audio_histogram.toString());
                    audioValues.put(NavigationContract.AudioObservations.KEY_OBSERVATION_DATE, current_date);
                    Uri audioUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.AudioObservations.TABLE_NAME);
                    getContentResolver().insert(audioUri, audioValues);

                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Sent audio_histogram: " + audio_histogram.toString());
                }

            } else {
                Log.d(Constants.DATA_COLLECTION_SERVICE, "Sensor: " + Constants.SENSOR_MICROPHONE + " is null");
            }

            return null;
        }
    }

    class SendWifi extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            final Location location = (Location) params[0];

            final String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

            if(mWifi != null) {

                mWifi.startScan(new SensorHotspotCallback() {
                    @Override
                    public void sendScanResults(List<ScanResult> scanResultList) {

                        Log.d(Constants.DATA_COLLECTION_SERVICE, "sendScanResults received callback with scanResultsList: " + scanResultList.toString());

                        ContentValues hotspotValues = new ContentValues();
                        hotspotValues.put(NavigationContract.HotspotObservations.KEY_LATITUDE, location.getLatitude());
                        hotspotValues.put(NavigationContract.HotspotObservations.KEY_LONGITUDE, location.getLongitude());
                        hotspotValues.put(NavigationContract.HotspotObservations.KEY_NUMBER_CONNECTED, scanResultList.size());
                        hotspotValues.put(NavigationContract.HotspotObservations.KEY_OBSERVATION_DATE, current_date);
                        Uri hotspotUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.HotspotObservations.TABLE_NAME);
                        getContentResolver().insert(hotspotUri, hotspotValues);

                        Log.d(Constants.DATA_COLLECTION_SERVICE, "Sent wifi count: " + scanResultList.size());
                    }
                });

            }

            return null;
        }
    }
}
