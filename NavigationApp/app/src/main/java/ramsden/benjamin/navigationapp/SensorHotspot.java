package ramsden.benjamin.navigationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
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
                    Toast.makeText(context, "wifiReceiver: null mCallback", Toast.LENGTH_SHORT).show();
                    return;
                }

                mCallback.sendScanResults(mWifiManager.getScanResults());

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

    public List<ScanResult> getScanResults() {
        return mScanResults;
    }

}
