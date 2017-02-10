package ramsden.benjamin.navigationapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ben on 10/02/2017.
 */

public class SensorBluetooth {

    private IntentFilter bluetoothFilter = new IntentFilter();
    private BluetoothAdapter bluetoothAdapter;

    private Integer count = 0;
    private Context mContext;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                String device = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                String rssi_msg = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

                Log.d(Constants.SENSOR_BLUETOOTH, "Found bluetooth device: " + device + " message: " + rssi_msg);
                count++;
            }
        }
    };

    public SensorBluetooth(Context mContext) {
        this.mContext = mContext;

        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /* TODO: need to deregister this receiver on destroy */
        mContext.registerReceiver(bluetoothReceiver, bluetoothFilter);
    }

    public void startDiscovery() {
        if(bluetoothAdapter != null) {
            count = 0;
            bluetoothAdapter.enable();
            bluetoothAdapter.startDiscovery();
        }
    }

    @Nullable
    public Integer getBluetoothDeviceCount() {
        return count;
    }
}
