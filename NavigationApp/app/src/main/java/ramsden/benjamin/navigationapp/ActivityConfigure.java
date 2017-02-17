package ramsden.benjamin.navigationapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.Toast;

public class ActivityConfigure extends AppCompatActivity {

    private DataCollectionService dataCollectionService = null;

    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Constants.ACTIVITY_CONFIGURE, "Service Connected to ActivityConfigure");

            DataCollectionService.MyBinder binder = (DataCollectionService.MyBinder) service;
            dataCollectionService = binder.getService();

            service_connection_ratingbar.setRating(1);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Constants.ACTIVITY_CONFIGURE, "Service Disconnected from ActivityConfigure");
            dataCollectionService = null;

            service_connection_ratingbar.setRating(0);
        }
    };

    private RatingBar service_connection_ratingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        service_connection_ratingbar = (RatingBar) findViewById(R.id.service_connection_ratingbar);

        /* Do not ask user for permissions in this activity, just let them know they are needed or wont work */
        checkPermissionsStartService(false);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if(dataCollectionService != null) {
            unbindService(locationServiceConnection);
        }

        /* TODO: Stop service stopping if it is foregrounded (recording background data) */
        stopService(new Intent(this, DataCollectionService.class));
    }

    /* Shows and hides the location text prompt in the Main Activity
     * Based on whether the permission have been granted or not to access location */
    private void checkPermissionsStartService(boolean requestIfNotGranted) {

        if(PermissionManager.checkAllPermissions(this, requestIfNotGranted) && dataCollectionService == null) {
            Log.d(Constants.PERMISSIONS, "All permissions granted: Launching dataCollectionService");

            /* Start the service and bind to it, ONLY ONCE PERMISSIONS ACQUIRED */
            Intent locationServiceIntent = new Intent(this, DataCollectionService.class);
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
