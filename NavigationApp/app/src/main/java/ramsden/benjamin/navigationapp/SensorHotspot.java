package ramsden.benjamin.navigationapp;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ben on 11/02/2017.
 */

public class SensorHotspot {

    private Context mContext;
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults;

    /* This variable is set by the start method, and unset by the broadcast receiver
     * In effect this means only 1 scan result is sent for a location after start() is called */
    private Location mLocation = null;

    public SensorHotspot(Context context) {
        mContext = context;

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mContext.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void start(Location location) {
        Log.d(Constants.SENSOR_HOTSPOT, "start");

        mLocation = location;

        mWifiManager.startScan(); /* TODO: Investigate is this needed, could wait for a system invoked one first, then if not trigger our own scan */
    }

    public void unregisterReceiver() {
        if(mContext != null && wifiReceiver != null) {
            Log.d(Constants.SENSOR_HOTSPOT, "unregistering wifiReceiver");
            mContext.unregisterReceiver(wifiReceiver);
        } else {
            Log.d(Constants.SENSOR_HOTSPOT, "failed to unregister wifiReceiver");
        }
    }

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                if(mLocation == null) {
                    Log.d(Constants.SENSOR_HOTSPOT, "wifiReceiver got SCAN_RESULTS_AVAILABLE_ACTION but mLocation is null, likely scan result is not app invoked");
                    return;
                }

                final String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

                mScanResults = mWifiManager.getScanResults();

                /* TODO: combine into one send (change api) */
                /* TODO: OR cache information already sent to avoid re-sending */
                for(ScanResult scanResult : mScanResults) {
                    ContentValues hotspotValues = new ContentValues();
                    hotspotValues.put(NavigationContract.Hotspots.KEY_SSID, scanResult.SSID);
                    hotspotValues.put(NavigationContract.Hotspots.KEY_SIGNAL_LEVEL, scanResult.level);
                    hotspotValues.put(NavigationContract.Hotspots.KEY_FREQUENCY, scanResult.frequency);
                    hotspotValues.put(NavigationContract.Hotspots.KEY_MAC, scanResult.BSSID);
                    hotspotValues.put(NavigationContract.Hotspots.KEY_REGISTER_DATE, current_date);
                    Uri hotspotUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.Hotspots.TABLE_NAME);
                    mContext.getContentResolver().insert(hotspotUri, hotspotValues);

                    Log.d(Constants.SENSOR_HOTSPOT, "Sent hotspot: " + scanResult.SSID);
                }

                Integer number_connected = 0; //not yet authorised to count ip's on local subnet

                ContentValues hotspotObservationValues = new ContentValues();
                hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_LATITUDE, mLocation.getLatitude());
                hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_LONGITUDE, mLocation.getLongitude());
                hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_NUMBER_CONNECTED, number_connected);
                hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_OBSERVATION_DATE, current_date);
                Uri hotspotObservationUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.HotspotObservations.TABLE_NAME);
                mContext.getContentResolver().insert(hotspotObservationUri, hotspotObservationValues);

                Log.d(Constants.SENSOR_HOTSPOT, "Sent hotspotObservation count: " + number_connected);

                /* Set location to null so future non-app invoked scan results do not get sent with outdated location data */
                mLocation = null;

            }
        }
    };

}
