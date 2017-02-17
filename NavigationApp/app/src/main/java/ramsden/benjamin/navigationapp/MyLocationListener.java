package ramsden.benjamin.navigationapp;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ben on 17/02/2017.
 */

class MyLocationListener implements LocationListener {

    DataCollectionService mDataCollectionService;

    public MyLocationListener(DataCollectionService dataCollectionService) {
        mDataCollectionService = dataCollectionService;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(Constants.MY_LOCATION_LISTENER,"onLocationChanged: " + location.toString());

        // collect Accelerometer Observations
        mDataCollectionService.startAccelerometer(location);

        // collect Audio Observations
        mDataCollectionService.startAudio(location);

        // collect Bluetooth Observations
        mDataCollectionService.startBluetooth(location);

        // collect Crowd Observations
        mDataCollectionService.startCrowd(location);

        // collect Hotspot Observations
        mDataCollectionService.startHotspot(location);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(Constants.MY_LOCATION_LISTENER,"onStatusChanged: " + status);

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(Constants.MY_LOCATION_LISTENER,"onProviderEnabled: " + provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(Constants.MY_LOCATION_LISTENER,"onProviderDisabled: " + provider);

    }
}
