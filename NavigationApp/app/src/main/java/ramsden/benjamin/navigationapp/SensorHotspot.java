package ramsden.benjamin.navigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by ben on 11/02/2017.
 */

public class SensorHotspot {

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                if(mCallback == null) {
                    Log.d(Constants.SENSOR_WIFI, "wifiReceiver got SCAN_RESULTS_AVAILABLE_ACTION but no mCallback set, likely scan result is not app invoked");
                    return;
                }

                Log.d(Constants.SENSOR_WIFI, "wifiReceiver sending scan results to mCallback");
                mCallback.sendScanResults(mWifiManager.getScanResults());

                /* Set callback to null so future non-app invoked scan results do not get sent with outdated location data */
                mCallback = null;

            }
        }
    };

    private Context mContext;
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults;

    private SensorHotspotCallback mCallback;

    public SensorHotspot(Context mContext) {
        this.mContext = mContext;

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        mContext.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void startScan(SensorHotspotCallback mCallback) {
        this.mCallback = mCallback;

        mWifiManager.startScan();
    }

}
