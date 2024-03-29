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
 * This class manages the collection of Hotspot data from the Wi-Fi module
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

                for(ScanResult scanResult : mScanResults) {
                    ContentValues hotspotObservationValues = new ContentValues();

                    /* Hotspot entry */
                    hotspotObservationValues.put(NavigationContract.Hotspots.KEY_SSID, scanResult.SSID);
                    hotspotObservationValues.put(NavigationContract.Hotspots.KEY_FREQUENCY, scanResult.frequency);
                    hotspotObservationValues.put(NavigationContract.Hotspots.KEY_MAC, scanResult.BSSID);

                    /* Hotspot Observation entry */
                    hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_LATITUDE, mLocation.getLatitude());
                    hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_LONGITUDE, mLocation.getLongitude());
                    hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_SIGNAL_LEVEL, scanResult.level);
                    hotspotObservationValues.put(NavigationContract.HotspotObservations.KEY_OBSERVATION_DATE, current_date);

                    Uri hotspotObservationUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.HotspotObservations.TABLE_NAME);
                    mContext.getContentResolver().insert(hotspotObservationUri, hotspotObservationValues);

                    Log.d(Constants.SENSOR_HOTSPOT, "Sent hotspot: " + scanResult.SSID + " with signal_level: " + scanResult.level);
                }

                /* Set location to null so future non-app invoked scan results do not get sent with outdated location data */
                mLocation = null;

            }
        }
    };

}
