package ramsden.benjamin.navigationapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.LocationListener;

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

    /* Provides the activities using the service the ability to
     * Retreive and Change the paramaters given to the location listener
     */
    private long minTime = Constants.DEFAULT_MIN_GPS_TIME;  //minimum time different between 2 location updates

    public long getMinTime() {
        return minTime;
    }

    private float minDistance = Constants.DEFAULT_MIN_GPS_DISTANCE;   //minimum distance between 2 location updates

    public float getMinDistance() {
        return minDistance;
    }

    public Location getLastLocation() {
        if (mGoogleApiClient == null) {
            return null;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    ;

    private final IBinder myBinder = new MyBinder();

    public class MyBinder extends Binder {
        ServiceDataCollection getService() {
            return ServiceDataCollection.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "ServiceDataCollection onBind");
        return myBinder;
    }

    private SharedPreferences sharedPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case Constants.PREFERENCE_MIN_DISTANCE:
                    minDistance = sharedPreferences.getFloat(Constants.PREFERENCE_MIN_DISTANCE, Constants.DEFAULT_MIN_GPS_DISTANCE);
                    Toast.makeText(getBaseContext(), "NOT YET WHILE RUNNING: ServiceDataCollection onSharedPreferenceChanged minDistance:" + minDistance, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.PREFERENCE_MIN_TIME:
                    minTime = sharedPreferences.getLong(Constants.PREFERENCE_MIN_TIME, Constants.DEFAULT_MIN_GPS_TIME);
                    Toast.makeText(getBaseContext(), "NOT YET WHILE RUNNING: ServiceDataCollection onSharedPreferenceChanged minTime:" + minTime, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL:
                    Long start_all_sensors_interval = sharedPreferences.getLong(Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL, Constants.DEFAULT_START_ALL_SENSORS_INTERVAL);
                    initSensorTimer(start_all_sensors_interval);
                    Toast.makeText(getBaseContext(), "ServiceDataCollection onSharedPreferenceChanged start_all_sensors_interval: " + start_all_sensors_interval, Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    private GoogleApiClient mGoogleApiClient;

    GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(getBaseContext(), "Connection to GoogleApiClient failed", Toast.LENGTH_SHORT).show();
            mGoogleApiClient = null;
        }
    };

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(Constants.GOOGLE_API_CLIENT, "ConnectionCallbacks onConnected");

            if (ActivityCompat.checkSelfPermission(ServiceDataCollection.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ServiceDataCollection.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if(mGoogleApiClient == null) {
                return;
            }

            LocationRequest locationRequest = new LocationRequest()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(minTime)
                    .setSmallestDisplacement(minDistance);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, locationListener);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(Constants.GOOGLE_API_CLIENT, "ConnectionCallbacks onConnectionSuspended");
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
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_runner_icon))
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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(connectionCallbacks)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();  //TODO: No auto manage, need to call connect and disconnect manually

        Long start_all_sensors_interval = sharedPreferences.getLong(Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL, Constants.DEFAULT_START_ALL_SENSORS_INTERVAL);

        initSensorTimer(start_all_sensors_interval);
    }

    private Timer sensor_timer;

    private void initSensorTimer(long start_all_sensors_interval) {
        if (sensor_timer != null) {
            sensor_timer.cancel();
            sensor_timer = null;
        }

        sensor_timer = new Timer();

        sensor_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(ServiceDataCollection.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ServiceDataCollection.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location != null) {
                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Started all sensors with location");
                    startAllSensors(location);
                } else {
                    Log.d(Constants.DATA_COLLECTION_SERVICE, "Could not start all sensors, location is null");
                }

            }
        }, start_all_sensors_interval, start_all_sensors_interval);
    }

    private void startAllSensors(Location location) {

        if (sensorAccelerometerManager != null) {
            sensorAccelerometerManager.startAccelerometer(location);
        }

        if (sensorAudioManager != null) {
            sensorAudioManager.startAudio(location);
        }

        if (sensorBluetoothManager != null) {
            sensorBluetoothManager.startBluetooth(location);
        }

        if (sensorHotspotManager != null) {
            sensorHotspotManager.startHotspot(location);
        }

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
        if (!notifActive) {
            startForeground(foregroudCode, foregroundNotif);
            notifActive = true;
        }
    }

    /* removes the foreground notificaiton if it does exist  */
    public void stopForegroundNotification() {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "ServiceDataCollection stopForegroundNotification");
        if (notifActive) {
            stopForeground(true);
            notifActive = false;
        }
    }

    /* Initializes the locationListener with the paramaters stored in the service
     * Can be called multiple times, if the locationListener has already been
     * initialized and is receiving updates, this method will request the location
     * manager remove updates the listener, and re-request the updates with the
     * Updated parameters */

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(Constants.DATA_COLLECTION_SERVICE, "onLocationChanged lat:" + location.getLatitude() + " lng:" + location.getLongitude());

            //TODO: Verify this is a valid way
            //Currently just logs that location update happened, getLastLocation is used to get the location
        }
    };

    /* Makes sure that the location listener is not still receiving updates
     * If the location service has been destroyed */
    @Override
    public void onDestroy() {
        Log.d(Constants.DATA_COLLECTION_SERVICE, "ServiceDataCollection onDestroy");
        Toast.makeText(getBaseContext(), "ServiceDataCollection onDestroy", Toast.LENGTH_SHORT).show();


        if(mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
            mGoogleApiClient.disconnect();
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
