package ramsden.benjamin.navigationapp;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Manages the applications LocationListener
 * Ensures that the location listener continues to provide the content provider with information
 * Can be made into a foreground notification at the users request, to stop it being killed after the app closes
 * It is assumed that when this is called the Location permission has been granted
 */

public class LocationService extends Service {

    private final IBinder myBinder = new MyBinder();

    private static final int pendingIntentCode = 333;
    private static final int foregroudCode = 343;
    private Notification foregroundNotif;
    private boolean notifActive = false;

    private MyLocationListener locationListener = null;

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

    public class MyBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("g53mdp","LocationService onBind");
        return myBinder;
    }

    /* Sets up the notification, and saves it in a variable ready to be used
     * Initializes the location listener */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("g53mdp", "LocationService onCreate");

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
        Log.d("g53mdp", "LocationService startForegroundNotification");
        if(!notifActive) {
            startForeground(foregroudCode, foregroundNotif);
            notifActive = true;
        }
    }

    /* removes the foreground notificaiton if it does exist  */
    public void stopForegroundNotification() {
        Log.d("g53mdp", "LocationService stopForegroundNotification");
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
            Toast.makeText(this, "Err: LocationService instantiated, without ACCESS_FINE_LOCATION Permission",Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("g53mdp", "initLocationListener called");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationListener != null) {
            /* Remove any previous location listener request */
            try {
                Log.d("g53mdp", "initLocationListener removed updates from locationListener");
                locationManager.removeUpdates(locationListener);
            } catch(SecurityException e) {
                Log.d("g53mdp", e.toString());
            }
        } else {
            /* Create instance of the location listener for the first time */
            locationListener = new MyLocationListener(this);
        }

        try {
            Log.d("g53mdp", "initLocationListener requested location updates with minTime: " + minTime + " minDistance: " + minDistance);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
        } catch(SecurityException e) {
            Log.d("g53mdp", e.toString());
        }
    }

    /* Makes sure that the location listener is not still receiving updates
     * If the location service has been destroyed */
    @Override
    public void onDestroy() {
        Log.d("g53mdp", "LocationService onDestroy");

        if(locationListener != null) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                Log.d("g53mdp", "onDestroy (LocationService) removed updates from locationListener");
                locationManager.removeUpdates(locationListener);
            } catch(SecurityException e) {
                Log.d("g53mdp", e.toString());
            }
        }

        super.onDestroy();
    }
}