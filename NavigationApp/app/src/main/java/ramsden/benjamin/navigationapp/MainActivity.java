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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final long DEFAULT_MIN_TIME = 1;
    public static final float DEFAULT_MIN_DISTANCE = 1f;

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 734;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 747;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 757;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 767;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 777;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE = 787;
    private static final int MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE = 797;

    /* Connection to the DataCollectionService
     * In the main activity this is purely used to ensure the DataCollectionService
     * Is destroyed upon exiting the application
     * Unless the user has specified the location tracking continue (foreground)
     * Through the options menu */
    private DataCollectionService dataCollectionService = null;
    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Constants.NAVIGATION_APP, "Service Connected to MainActivity");

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
                startActivity(new Intent(MainActivity.this, APITestActivity.class));
            }
        });

        Button sent_log_button = (Button) findViewById(R.id.sent_log_button);
        sent_log_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SentLogActivity.class));
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





    /* Shows and hides the location text prompt in the Main Activity
     * Based on whether the permission have been granted or not to access location */
    private void checkPermissionsStartService(boolean requestIfNotGranted) {
        boolean bootservice = true;

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got ACCESS_FINE_LOCATION permission");

        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting ACCESS_FINE_LOCATION permission");

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied ACCESS_FINE_LOCATION permission");

            Toast.makeText(this, "This application cannot function without the ACCESS_FINE_LOCATION permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            bootservice = false;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got RECORD_AUDIO permission");

        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting RECORD_AUDIO permission");

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied RECORD_AUDIO permission");

            Toast.makeText(this, "This application cannot function without the RECORD_AUDIO permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            bootservice = false;
        }


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got BLUETOOTH permission");

        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting BLUETOOTH permission");

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, MY_PERMISSIONS_REQUEST_BLUETOOTH);
            return;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied BLUETOOTH permission");

            Toast.makeText(this, "This application cannot function without the BLUETOOTH permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            bootservice = false;
        }


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got BLUETOOTH_ADMIN permission");

        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting BLUETOOTH_ADMIN permission");

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);
            return;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied BLUETOOTH_ADMIN permission");

            Toast.makeText(this, "This application cannot function without the BLUETOOTH_ADMIN permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            bootservice = false;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got WRITE_EXTERNAL_STORAGE permission");

        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting WRITE_EXTERNAL_STORAGE permission");

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied WRITE_EXTERNAL_STORAGE permission");

            Toast.makeText(this, "This application cannot function without the WRITE_EXTERNAL_STORAGE permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            bootservice = false;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got ACCESS_WIFI_STATE permission");

        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting ACCESS_WIFI_STATE permission");

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE);
            return;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied ACCESS_WIFI_STATE permission");

            Toast.makeText(this, "This application cannot function without the ACCESS_WIFI_STATE permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            bootservice = false;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got CHANGE_WIFI_STATE permission");

        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting CHANGE_WIFI_STATE permission");

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE);
            return;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied CHANGE_WIFI_STATE permission");

            Toast.makeText(this, "This application cannot function without the CHANGE_WIFI_STATE permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            bootservice = false;
        }

        if(bootservice && dataCollectionService == null) {
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
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO:
            case MY_PERMISSIONS_REQUEST_BLUETOOTH:
            case MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN:
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            case MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE:
            case MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE:
                checkPermissionsStartService(false);
                break;
            default:
                Toast.makeText(MainActivity.this, "onRequestPermissionResult case not set for requestCode " + requestCode, Toast.LENGTH_LONG).show();
        }

    }
}
