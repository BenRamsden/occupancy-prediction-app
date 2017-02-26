package ramsden.benjamin.navigationapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class ActivityConfigure extends AppCompatActivity {

    private ServiceDataCollection serviceDataCollection = null;

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Constants.ACTIVITY_CONFIGURE, "Service Connected to ActivityConfigure");

            ServiceDataCollection.MyBinder binder = (ServiceDataCollection.MyBinder) service;
            serviceDataCollection = binder.getService();

            service_connected_checkbox.setChecked(true);
            foreground_service_switch.setChecked(serviceDataCollection.isForegroundNotification());
            minimum_location_distance_edittext.setText(String.valueOf( (int) serviceDataCollection.getMinDistance()));
            minimum_location_time_edittext.setText(String.valueOf(serviceDataCollection.getMinTime()));

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Constants.ACTIVITY_CONFIGURE, "Service Disconnected from ActivityConfigure");
            serviceDataCollection = null;

            service_connected_checkbox.setChecked(false);
            foreground_service_switch.setChecked(false);
            minimum_location_time_edittext.setText("");
            minimum_location_distance_edittext.setText("");
        }
    };

    private CheckBox service_connected_checkbox;
    private Switch foreground_service_switch;
    private EditText minimum_location_distance_edittext;
    private EditText minimum_location_time_edittext;

    private SharedPreferences sharedPreferences;

    private EditText map_polling_edittext;
    private EditText sensor_polling_edittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        sharedPreferences = this.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        service_connected_checkbox = (CheckBox) findViewById(R.id.service_connected_checkbox);

        minimum_location_distance_edittext = (EditText) findViewById(R.id.minimum_location_distance_edittext);
        minimum_location_time_edittext = (EditText) findViewById(R.id.minimum_location_time_edittext);

        map_polling_edittext = (EditText) findViewById(R.id.map_polling_edittext);
        map_polling_edittext.setText( String.valueOf(sharedPreferences.getLong(Constants.PREFERENCE_MAP_POLL_INTERVAL, Constants.DEFAULT_MAP_POLL_INTERVAL)) );

        final Button map_polling_submit = (Button) findViewById(R.id.map_polling_submit);
        map_polling_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String map_polling_interval_str = map_polling_edittext.getText().toString();
                Long map_polling_interval;

                try {
                    map_polling_interval = Long.valueOf(map_polling_interval_str);
                } catch(NumberFormatException ex) {
                    Toast.makeText(ActivityConfigure.this, "Map Polling Interval Format Invalid, not updated", Toast.LENGTH_SHORT).show();
                    return;
                }

                sharedPreferences.edit().putLong(Constants.PREFERENCE_MAP_POLL_INTERVAL, map_polling_interval).commit();
            }
        });

        sensor_polling_edittext = (EditText) findViewById(R.id.sensor_polling_edittext);
        sensor_polling_edittext.setText( String.valueOf( sharedPreferences.getLong(Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL, Constants.DEFAULT_START_ALL_SENSORS_INTERVAL) ) );

        final Button sensor_polling_submit = (Button) findViewById(R.id.sensor_polling_submit);
        sensor_polling_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sensor_polling_interval_str = sensor_polling_edittext.getText().toString();
                Long sensor_polling_interval;

                try {
                    sensor_polling_interval = Long.valueOf(sensor_polling_interval_str);
                } catch(NumberFormatException ex) {
                    Toast.makeText(ActivityConfigure.this, "Sensor Polling Interval Format Invalid, not updated", Toast.LENGTH_SHORT).show();
                    return;
                }

                sharedPreferences.edit().putLong(Constants.PREFERENCE_START_ALL_SENSORS_INTERVAL, sensor_polling_interval).commit();
            }
        });

        final Button reset_all_prefs_button = (Button) findViewById(R.id.reset_all_prefs_button);
        reset_all_prefs_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().clear().commit();
                Toast.makeText(ActivityConfigure.this, "All user preferences cleared", Toast.LENGTH_SHORT).show();
            }
        });

        final Button location_distance_submit = (Button) findViewById(R.id.location_distance_submit);
        location_distance_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String min_distance_str = minimum_location_distance_edittext.getText().toString();
                Float min_distance;

                try {
                    min_distance = Float.parseFloat(min_distance_str);
                } catch(NumberFormatException ex) {
                    Toast.makeText(ActivityConfigure.this, "One of the parameters is invalid, cannot update", Toast.LENGTH_SHORT).show();
                    return;
                }

                sharedPreferences.edit()
                        .putFloat(Constants.PREFERENCE_MIN_DISTANCE, min_distance)
                        .commit();

            }
        });

        final Button location_time_submit = (Button) findViewById(R.id.location_time_submit);
        location_time_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String min_time_str = minimum_location_time_edittext.getText().toString();
                Long min_time;

                try {
                    min_time = Long.parseLong(min_time_str);
                } catch(NumberFormatException ex) {
                    Toast.makeText(ActivityConfigure.this, "One of the parameters is invalid, cannot update", Toast.LENGTH_SHORT).show();
                    return;
                }

                sharedPreferences.edit()
                        .putLong(Constants.PREFERENCE_MIN_TIME, min_time)
                        .commit();

            }
        });

        foreground_service_switch = (Switch) findViewById(R.id.foreground_service_switch);
        foreground_service_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(serviceDataCollection == null) {
                    Toast.makeText(ActivityConfigure.this, "Cannot adjust service settings, service not connected", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isChecked) {
                    Toast.makeText(ActivityConfigure.this, "Service foregrounded", Toast.LENGTH_SHORT).show();
                    serviceDataCollection.startForegroundNotification();
                } else {
                    Toast.makeText(ActivityConfigure.this, "Service backgrounded", Toast.LENGTH_SHORT).show();
                    serviceDataCollection.stopForegroundNotification();
                }

            }
        });

        /* Do not ask user for permissions in this activity, just let them know they are needed or wont work */
        checkPermissionsStartService(false);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if(serviceDataCollection != null) {
            unbindService(locationServiceConnection);
        }
    }

    /* Shows and hides the location text prompt in the Main Activity
     * Based on whether the permission have been granted or not to access location */
    private void checkPermissionsStartService(boolean requestIfNotGranted) {

        if(PermissionManager.checkAllPermissions(this, requestIfNotGranted) && serviceDataCollection == null) {
            Log.d(Constants.PERMISSIONS, "All permissions granted: Launching serviceDataCollection");

            /* Start the service and bind to it, ONLY ONCE PERMISSIONS ACQUIRED */
            Intent locationServiceIntent = new Intent(this, ServiceDataCollection.class);
            startService(locationServiceIntent);
            bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /* Receives the callback from the OS after the user has clicked Allow location permission
     * This calls the UI update method to remove the prompt to the user,
     * Also sets the map to use the users location now available */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionManager.isManagedPermission(requestCode)) {
            /* Do not ask user for permissions in this activity, just let them know they are needed or wont work */
            checkPermissionsStartService(false);
        } else {
            Toast.makeText(ActivityConfigure.this, "onRequestPermissionResult case not set for requestCode " + requestCode, Toast.LENGTH_LONG).show();
        }

    }

}
