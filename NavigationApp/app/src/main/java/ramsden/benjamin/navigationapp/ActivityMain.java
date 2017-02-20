package ramsden.benjamin.navigationapp;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
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

    GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(ActivityMain.this, "Connection to GoogleApiClient failed", Toast.LENGTH_SHORT).show();
            mGoogleApiClient = null;
        }
    };

    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(Constants.GOOGLE_API_CLIENT, "ConnectionCallbacks onConnected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(Constants.GOOGLE_API_CLIENT, "ConnectionCallbacks onConnectionSuspended");
        }
    };

    public static final String OCCUPANCY_ESTIMATE_RECEIVER = "ramsden.benjamin.navigationapp.ActivityMain.occupancyEstimateReceiver";

    private final BroadcastReceiver occupancyEstimateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(ActivityMain.this, "OccupancyEstimateReceiver onReceive", Toast.LENGTH_SHORT).show();

            String occupancy_estimate = intent.getStringExtra("occupancy_estimate");

            showCrowdObservationAlertDialog(occupancy_estimate);
        }
    };
    
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        onConnectionFailedListener)
                .addConnectionCallbacks(connectionCallbacks)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        /* Get response from request for occupancy estimation */
        registerReceiver(occupancyEstimateReceiver, new IntentFilter(OCCUPANCY_ESTIMATE_RECEIVER));
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
        if (mMap != null && !mMap.isMyLocationEnabled()) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.d(Constants.NAVIGATION_APP, e.getMessage());
            }
        }
    }

    public void findLikelyPlaces(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ActivityMain.this, "Cannot findLikelyPlaces, no ACCESS_FINE_LOCATION permission", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mGoogleApiClient == null) {
            Toast.makeText(ActivityMain.this, "Cannot find likely places, mGoogleApiClient is null", Toast.LENGTH_SHORT).show();
            Log.d(Constants.GOOGLE_API_CLIENT, "Cannot find likely places, mGoogleApiClient is null");
            return;
        }

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);

        final int mMaxEntries = 5;

        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                Toast.makeText(ActivityMain.this, "onResult", Toast.LENGTH_SHORT).show();
                int i = 0;

                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    // Build a list of likely places to show the user. Max 5.
                    Toast.makeText(ActivityMain.this, "Got likely place " + placeLikelihood.getPlace().toString(), Toast.LENGTH_SHORT).show();

                    i++;
                    if (i > (mMaxEntries - 1)) {
                        break;
                    }
                }
                // Release the place likelihood buffer, to avoid memory leaks.
                likelyPlaces.release();

                if(likelyPlaces.getCount() == 0) {
                    Toast.makeText(ActivityMain.this, "Likely Places are 0, not a public destination", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void giveCrowdObservation(View v) {

        if(dataCollectionService == null) {
            Toast.makeText(ActivityMain.this, "Data Collection Service has not started yet, please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        Location lastLocation = dataCollectionService.getLastLocation();

        if(lastLocation == null) {
            Toast.makeText(ActivityMain.this, "App has not received a location update yet, please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse( NavigationContentProvider.CONTENT_URI + "/OCCUPANCY_ESTIMATE");
        ContentValues contentValues = new ContentValues();
        contentValues.put("lat",lastLocation.getLatitude());
        contentValues.put("lng",lastLocation.getLongitude());
        getContentResolver().insert(uri, contentValues);

        /* Broadcast Receiver calls showCrowdObservationsAlertDialog if server returns occupancy estimate */
    }

    private void showCrowdObservationAlertDialog(String occupancy_estimate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crowd Observation");

        builder.setTitle("We estimate " + occupancy_estimate + " are at your current location, please enter your estimate");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mString = input.getText().toString();
                Integer user_estimate = Integer.parseInt(mString);

                if(dataCollectionService != null && dataCollectionService.sendCrowdObservation(user_estimate)) {
                    Toast.makeText(ActivityMain.this, "Thank you for your estimate of " + user_estimate + " we will use this to make our service better!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityMain.this, "Error sending your estimate, Data Collection Service is not initialized", Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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

        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        unregisterReceiver(occupancyEstimateReceiver);
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

        if (grantResults.length > 0 && PermissionManager.isManagedPermission(requestCode)) {
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
