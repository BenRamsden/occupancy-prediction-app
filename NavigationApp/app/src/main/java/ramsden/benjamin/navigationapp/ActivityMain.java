package ramsden.benjamin.navigationapp;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ActivityMain extends AppCompatActivity implements OnMapReadyCallback {

    /* Connection to the DataCollectionService
     * In the main activity this is purely used to ensure the DataCollectionService
     * Is destroyed upon exiting the application
     * Unless the user has specified the location tracking continue (foreground)
     * Through the options menu */
    private DataCollectionService dataCollectionService = null;
    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Constants.NAVIGATION_APP, "Service Connected to ActivityMain");

            DataCollectionService.MyBinder binder = (DataCollectionService.MyBinder) service;
            dataCollectionService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Constants.NAVIGATION_APP, "Service Disconnected from ActivityMain");

            dataCollectionService = null;
        }
    };



    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button api_test_button = (Button) findViewById(R.id.api_test_button);
        api_test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityMain.this, ActivityAPITest.class));
            }
        });

        Button sent_log_button = (Button) findViewById(R.id.sent_log_button);
        sent_log_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityMain.this, ActivitySentLog.class));
            }
        });

        Button configure_button = (Button) findViewById(R.id.configure_button);
        configure_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityMain.this, ActivityConfigure.class));
            }
        });

        checkPermissionsStartService(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(52.953357, -1.18736);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker at Computer Science, Nottingham University"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        enableMapMyLocation();
    }


    public void enableMapMyLocation() {
        /* Try enable mMap location now user has responded to permission request
        * If not, error will be caught and dealt with here */
        if(mMap != null && !mMap.isMyLocationEnabled()) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.d(Constants.NAVIGATION_APP, e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        boolean foreground_service = false;

        if(dataCollectionService != null) {
            foreground_service = dataCollectionService.isForegroundNotification();

            unbindService(locationServiceConnection);
        }
        
        if(!foreground_service) {
            stopService(new Intent(this, DataCollectionService.class));
        }
    }

    /* Shows and hides the location text prompt in the Main Activity
     * Based on whether the permission have been granted or not to access location */
    private void checkPermissionsStartService(boolean requestIfNotGranted) {

        if(PermissionManager.checkAllPermissions(this, requestIfNotGranted) && dataCollectionService == null) {
            Log.d(Constants.PERMISSIONS, "All permissions granted: Launching dataCollectionService");

            enableMapMyLocation();

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
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsStartService(true);
            } else {
                /* Do not keep asking user for that permission if they denied the first time */
                checkPermissionsStartService(false);
            }
        } else {
            Toast.makeText(ActivityMain.this, "onRequestPermissionResult case not set for requestCode " + requestCode, Toast.LENGTH_LONG).show();
        }

    }
}
