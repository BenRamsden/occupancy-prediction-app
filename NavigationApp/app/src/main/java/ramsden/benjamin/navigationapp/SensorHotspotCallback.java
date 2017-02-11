package ramsden.benjamin.navigationapp;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by ben on 11/02/2017.
 */

public interface SensorHotspotCallback {

    void sendScanResults(List<ScanResult> scanResultList);

}
