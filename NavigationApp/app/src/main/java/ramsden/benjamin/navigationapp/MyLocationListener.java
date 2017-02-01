package ramsden.benjamin.navigationapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by ben on 01/02/2017.
 */

public class MyLocationListener implements LocationListener {

    private Context myContext;

    /* This class requires context to be able to broadcast */
    public MyLocationListener(Context context) {
        if(context != null) {
            myContext = context;
        } else {
            throw new Error("MyLocationListener received null context");
        }
    }

    /* Receives the callback from the OS when the device location has changes
     * Passes on the data to the contentprovider and local broadcasts it */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLocationChanged(Location location) {

        Uri queryUri = LocationContentProvider.CONTENT_URI;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = sdf.format(new Date());

        /* Send Location update to ContentProvider to be inserted */
        ContentValues contentValues = new ContentValues();
        contentValues.put(NavigationServerContract.LocationLogEntry.KEY_DATETIME, currentDateTime);
        contentValues.put(NavigationServerContract.LocationLogEntry.KEY_LATITUDE, location.getLatitude());
        contentValues.put(NavigationServerContract.LocationLogEntry.KEY_LONGITUDE, location.getLongitude());
        contentValues.put(NavigationServerContract.LocationLogEntry.KEY_ALTITUDE, location.getAltitude());
        myContext.getContentResolver().insert(queryUri,contentValues);

        /* Send Location update by LocalBroadcast for UI elements to pick up on */
        Intent broadcastIntent = new Intent(MainActivity.LOCATION_SERVICE_INTENT_FILTER);
        broadcastIntent.putExtra(NavigationServerContract.LocationLogEntry.KEY_DATETIME, currentDateTime);
        broadcastIntent.putExtra(NavigationServerContract.LocationLogEntry.KEY_LATITUDE, location.getLatitude());
        broadcastIntent.putExtra(NavigationServerContract.LocationLogEntry.KEY_LONGITUDE, location.getLongitude());
        broadcastIntent.putExtra(NavigationServerContract.LocationLogEntry.KEY_ALTITUDE, location.getAltitude());
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(broadcastIntent);

        Log.d("g53mdp", "onLocationChanged lat:"+location.getLatitude() + " lng:" + location.getLongitude() + " alt:" + location.getAltitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // information about the signal, i.e. number of satellites
        Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // the user enabled (for example) the GPS
        Log.d("g53mdp", "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // the user disabled (for example) the GPS
        Log.d("g53mdp", "onProviderDisabled: " + provider);
    }
}
