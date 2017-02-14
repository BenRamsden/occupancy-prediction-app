package ramsden.benjamin.navigationapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ben on 10/02/2017.
 */

public class SensorBluetooth {

    private IntentFilter bluetoothFilter = new IntentFilter();
    private BluetoothAdapter bluetoothAdapter;

    private Integer count = 0;
    private Context mContext;
    private Location mLocation;

    private SensorBluetoothAsyncTask sensorBluetoothAsyncTask;

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

    public SensorBluetooth(Context context) {
        mContext = context;

        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /* TODO: need to deregister this receiver on destroy */
        mContext.registerReceiver(bluetoothReceiver, bluetoothFilter);
    }

    public void start(Location location) {
        Log.d(Constants.SENSOR_BLUETOOTH, "start");

        if(sensorBluetoothAsyncTask != null && sensorBluetoothAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(Constants.SENSOR_BLUETOOTH, "sensorBluetoothAsyncTask has status RUNNING, refused to start another");
            return;
        }

        mLocation = location;

        sensorBluetoothAsyncTask = new SensorBluetoothAsyncTask();

        sensorBluetoothAsyncTask.execute();
    }

    @Nullable
    public Integer getBluetoothDeviceCount() {
        return count;
    }

    public void unregisterReceiver() {
        if(mContext != null && bluetoothReceiver != null) {
            Log.d(Constants.SENSOR_BLUETOOTH, "unregistering bluetoothReceiver");
            mContext.unregisterReceiver(bluetoothReceiver);
        } else {
            Log.d(Constants.SENSOR_BLUETOOTH, "failed to unregister bluetoothReceiver");
        }
    }

    class SensorBluetoothAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            if(bluetoothAdapter == null) {
                Log.d(Constants.SENSOR_BLUETOOTH, "bluetoothAdapter is null, cannot start bluetooth scan");
                return null;
            }

            count = 0;
            bluetoothAdapter.enable();
            bluetoothAdapter.startDiscovery();

            int max_bluetooth_polls = 300;
            int bluetooth_polls = 0;

            Integer bluetooth_count = 0;
            int consistent_count = 0;

            while(consistent_count < 100 && bluetooth_polls < max_bluetooth_polls) {
                Integer temp_bluetooth_count = getBluetoothDeviceCount();

                if(temp_bluetooth_count == bluetooth_count) {
                    consistent_count++;
                } else {
                    consistent_count = 0;
                    bluetooth_count = temp_bluetooth_count;
                }

                //Log.d(Constants.SENSOR_BLUETOOTH, "bluetooth_count: " + bluetooth_count + " consistent_count: " + consistent_count + " bluetooth_polls: " + bluetooth_polls);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                bluetooth_polls++;
            }


            if(bluetooth_count == null) {
                Log.d(Constants.SENSOR_BLUETOOTH, "Result: bluetooth_count is null");
            } else {
                String current_date = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

                ContentValues bluetoothValues = new ContentValues();
                bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_LATITUDE, mLocation.getLatitude());
                bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_LONGITUDE, mLocation.getLongitude());
                bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_BLUETOOTH_COUNT, bluetooth_count);
                bluetoothValues.put(NavigationContract.BluetoothObservations.KEY_OBSERVATION_DATE, current_date);
                Uri bluetoothUri = Uri.parse(NavigationContentProvider.CONTENT_URI + "/" + NavigationContract.BluetoothObservations.TABLE_NAME);
                mContext.getContentResolver().insert(bluetoothUri, bluetoothValues);

                Log.d(Constants.SENSOR_BLUETOOTH, "Sent bluetooth_count: " + bluetooth_count);
            }
            return null;
        }
    }

}
