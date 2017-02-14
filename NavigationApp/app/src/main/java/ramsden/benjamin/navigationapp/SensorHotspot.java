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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ben on 11/02/2017.
 */

public class SensorHotspot {

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                if(mLocation == null) {
                    Log.d(Constants.SENSOR_WIFI, "wifiReceiver got SCAN_RESULTS_AVAILABLE_ACTION but mLocation is null, likely scan result is not app invoked");
                    return;
                }

                final String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

                mScanResults = mWifiManager.getScanResults();

                ContentValues hotspotValues = new ContentValues();
                hotspotValues.put(NavigationContract.HotspotObservations.KEY_LATITUDE, mLocation.getLatitude());
                hotspotValues.put(NavigationContract.HotspotObservations.KEY_LONGITUDE, mLocation.getLongitude());
                hotspotValues.put(NavigationContract.HotspotObservations.KEY_NUMBER_CONNECTED, mScanResults.size());
                hotspotValues.put(NavigationContract.HotspotObservations.KEY_OBSERVATION_DATE, current_date);
                Uri hotspotUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.HotspotObservations.TABLE_NAME);
                mContext.getContentResolver().insert(hotspotUri, hotspotValues);

                Log.d(Constants.SENSOR_WIFI, "Sent wifi count: " + mScanResults.size());

                /* Set callback to null so future non-app invoked scan results do not get sent with outdated location data */
                mLocation = null;

            }
        }
    };

    private Context mContext;
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults;

    private Location mLocation = null;

    public SensorHotspot(Context context) {
        mContext = context;

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mContext.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void start(Location location) {
        mLocation = location;

        mWifiManager.startScan(); /* TODO: Investigate is this needed, could wait for a system invoked one first, then if not trigger our own scan */
    }

    public void unregisterReceiver() {
        if(mContext != null && wifiReceiver != null) {
            Log.d(Constants.SENSOR_WIFI, "unregistering wifiReceiver");
            mContext.unregisterReceiver(wifiReceiver);
        } else {
            Log.d(Constants.SENSOR_WIFI, "failed to unregister wifiReceiver");
        }
    }

}
