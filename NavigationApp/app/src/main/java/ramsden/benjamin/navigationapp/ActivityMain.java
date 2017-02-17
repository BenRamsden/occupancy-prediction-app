package ramsden.benjamin.navigationapp;

import android.Manifest;
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

        checkPermissionsStartService(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
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

        if(dataCollectionService != null) {
            unbindService(locationServiceConnection);
        }

        /* TODO: Stop service stopping if it is foregrounded (recording background data) */
        stopService(new Intent(this, DataCollectionService.class));
    }


    public boolean requestPermission(String permission, int requestCode, boolean requestIfNotGranted) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got " + permission + " permission");
            return true;
        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting " + permission + " permission");

            ActivityCompat.requestPermissions(this, new String[]{ permission }, Constants.MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE);
            return false;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied " + permission + " permission");

            Toast.makeText(this, "This application cannot function without the " + permission + " permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            return false;
        }

    }


    /* Shows and hides the location text prompt in the Main Activity
     * Based on whether the permission have been granted or not to access location */
    private void checkPermissionsStartService(boolean requestIfNotGranted) {

        if(! requestPermission( Manifest.permission.ACCESS_FINE_LOCATION, Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION, requestIfNotGranted)) {
            return;
        };

        if(! requestPermission(Manifest.permission.RECORD_AUDIO, Constants.MY_PERMISSIONS_REQUEST_RECORD_AUDIO, requestIfNotGranted)) {
            return;
        };

        if(! requestPermission(Manifest.permission.BLUETOOTH, Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH, requestIfNotGranted)) {
            return;
        };

        if(! requestPermission(Manifest.permission.BLUETOOTH_ADMIN, Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN, requestIfNotGranted)) {
            return;
        };

        if(! requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE, requestIfNotGranted)) {
            return;
        };

        if(! requestPermission(Manifest.permission.ACCESS_WIFI_STATE, Constants.MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE, requestIfNotGranted)) {
            return;
        };

        if(! requestPermission(Manifest.permission.CHANGE_WIFI_STATE, Constants.MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE, requestIfNotGranted)) {
            return;
        };

        if(dataCollectionService == null) {
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

        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION:
            case Constants.MY_PERMISSIONS_REQUEST_RECORD_AUDIO:
            case Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH:
            case Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN:
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            case Constants.MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE:
            case Constants.MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE:
                checkPermissionsStartService(true);
                break;
            default:
                Toast.makeText(ActivityMain.this, "onRequestPermissionResult case not set for requestCode " + requestCode, Toast.LENGTH_LONG).show();
        }

    }
}
