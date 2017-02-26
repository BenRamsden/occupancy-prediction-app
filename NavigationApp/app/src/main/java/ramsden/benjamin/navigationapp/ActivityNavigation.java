package ramsden.benjamin.navigationapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityNavigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    /* Connection to the ServiceDataCollection
 * In the main activity this is purely used to ensure the ServiceDataCollection
 * Is destroyed upon exiting the application
 * Unless the user has specified the location tracking continue (foreground)
 * Through the options menu */
    private ServiceDataCollection serviceDataCollection = null;
    private ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Constants.NAVIGATION_APP, "Service Connected to ActivityNavigation");

            ServiceDataCollection.MyBinder binder = (ServiceDataCollection.MyBinder) service;
            serviceDataCollection = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Constants.NAVIGATION_APP, "Service Disconnected from ActivityNavigation");

            serviceDataCollection = null;
        }
    };

    GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(ActivityNavigation.this, "Connection to GoogleApiClient failed", Toast.LENGTH_SHORT).show();
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

    public static final String OCCUPANCY_ESTIMATE_RECEIVER = "ramsden.benjamin.navigationapp.ActivityNavigation.occupancyEstimateReceiver";

    final int opacity_multiplier = 3;
    final double lat_target_offset = 0.0007d;
    final double lng_target_offset = 0.0010d;
    final double lat_increment = 0.00023d;
    final double lng_increment = 0.00035d;
    final int max_opacity = 150;

    private HashMap<String, Float> occupancy_square_id_to_occupancy = new HashMap<>();

    private ArrayList<Polygon> occupancy_squares = new ArrayList<>();

    private final BroadcastReceiver occupancyEstimateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mode = intent.getStringExtra(NavigationContract.OccupancyEstimate.EXTRA_MODE);

            //Toast.makeText(ActivityNavigation.this, "OccupancyEstimateReceiver onReceive, mode: " + mode, Toast.LENGTH_SHORT).show();

            switch (mode) {
                case CROWD_OBSERVATION_MODE:
                    String occupancy_estimate = intent.getStringExtra(NavigationContract.OccupancyEstimate.EXTRA_OCCUPANCY_ESTIMATE);

                    showCrowdObservationAlertDialog(occupancy_estimate);
                    break;
                case MAP_POLL_MODE:
                    if (mMap == null) {
                        Toast.makeText(ActivityNavigation.this, "Cannot draw occupancy, map is null", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (Polygon drawn_square : occupancy_squares) {
                        //Log.d(Constants.NAVIGATION_APP, "Drawn Circles Array: Removing circle " + drawn_circle.getId());
                        drawn_square.remove();
                    }
                    occupancy_squares.clear();
                    occupancy_square_id_to_occupancy.clear();

                    String lat_lng_occupancy_list = intent.getStringExtra(NavigationContract.OccupancyEstimateBulk.EXTRA_LAT_LNG_OCCUPANCY_LIST);
                    JSONObject jsonObject;

                    try {
                        jsonObject = new JSONObject(lat_lng_occupancy_list);
                    } catch (JSONException e) {
                        Toast.makeText(ActivityNavigation.this, "lat_lng_occupancy_list was invalid JSON", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Toast.makeText(ActivityNavigation.this, "Got map poll back", Toast.LENGTH_SHORT).show();
                    Log.d(Constants.NAVIGATION_APP, "Got map poll back");

                    Iterator<String> iterator = jsonObject.keys();

                    while (iterator.hasNext()) {
                        JSONObject lat_lng_occupancy;

                        try {
                            lat_lng_occupancy = jsonObject.getJSONObject(iterator.next());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            continue;
                        }

                        //Toast.makeText(ActivityNavigation.this, "Got json object: " + lat_lng_occupancy.toString(), Toast.LENGTH_SHORT).show();
                        Double lat, lng;
                        String occupancy_str;
                        try {
                            lat = lat_lng_occupancy.getDouble(NavigationContract.OccupancyEstimate.ARG_LAT);
                            lng = lat_lng_occupancy.getDouble(NavigationContract.OccupancyEstimate.ARG_LNG);
                            occupancy_str = lat_lng_occupancy.getString("occupancy");
                        } catch (JSONException e) {
                            Log.d(Constants.NAVIGATION_APP, "Doubles lat, lng, occupancy could not all be parsed; possibly occupancy was null");
                            continue;
                        }

                        final double square_size = 0.4d;

                        LatLng point1 = new LatLng(lat-(square_size*lat_increment), lng+(square_size*lng_increment));
                        LatLng point2 = new LatLng(lat+(square_size*lat_increment), lng+(square_size*lng_increment));
                        LatLng point3 = new LatLng(lat+(square_size*lat_increment), lng-(square_size*lng_increment));
                        LatLng point4 = new LatLng(lat-(square_size*lat_increment), lng-(square_size*lng_increment));

                        Float occupancy;
                        try {
                            occupancy = Float.parseFloat(occupancy_str);
                        } catch (NumberFormatException ex) {
                            //Log.d(Constants.NAVIGATION_APP, "String " + occupancy_str + " not a valid float");
                            //Expected to happen if occupancy estimate for that region is null
//                            Polygon polygon = mMap.addPolygon(new PolygonOptions().add(point1).add(point2).add(point3).add(point4)
//                                    .strokeColor(Color.TRANSPARENT)
//                                    .fillColor(Color.argb(50, 100, 100, 100))
//                                    .clickable(true));
//
//                            occupancy_squares.add(polygon);

                            continue;
                        }

                        int red = 0;
                        int green = 0;
                        int blue = 0;
                        int opacity = 0;

                        if (occupancy > 30) {
                            red = 255;
                        } else if(occupancy > 15) {
                            red = 255;
                            green = 165;
                        } else {
                            green = 150;
                        }

                        opacity = Math.min(max_opacity, Math.round(occupancy) * opacity_multiplier + 20);

                        //Log.d("Color", String.valueOf(opacity));

                        Polygon polygon = mMap.addPolygon(new PolygonOptions().add(point1).add(point2).add(point3).add(point4)
                            .strokeColor(Color.TRANSPARENT)
                            .fillColor(Color.argb(opacity, red, green, blue))
                            .clickable(true));

                        occupancy_square_id_to_occupancy.put(polygon.getId(), occupancy);

                        occupancy_squares.add(polygon);

                    }
                    break;

            }


        }
    };

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

    private Timer mapPollTimer;
    private long mMapPollInterval;

    private Timer cameraCenterTimer;
    private long mCameraCenterInterval = 1000;

    private LatLng last_camera_center;

    private SharedPreferences sharedPreferences;

    private void refreshSharedPreferences(boolean print_out) {
        //if(print_out) Toast.makeText(this, "refreshSharedPreferences called with print_out", Toast.LENGTH_SHORT).show();

        long mMapPollInterval = sharedPreferences.getLong(Constants.PREFERENCE_MAP_POLL_INTERVAL, Constants.DEFAULT_MAP_POLL_INTERVAL);

        if(this.mMapPollInterval != mMapPollInterval) {
            if(print_out) Toast.makeText(this, "mMapPollInterval: Changed to " + mMapPollInterval, Toast.LENGTH_SHORT).show();
            this.mMapPollInterval = mMapPollInterval;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /***** SHARED PREFERENCES SET UP */

        sharedPreferences = this.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        /***** DRAWER SET UP */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /***** GOOGLE MAP SET UP */

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
    public void onPause() {
        super.onPause();

        if(mapPollTimer != null) {
            Log.d(Constants.NAVIGATION_APP, "onPause, cancelling mapPollTimer");
            mapPollTimer.cancel();
        }

        if(cameraCenterTimer != null) {
            Log.d(Constants.NAVIGATION_APP, "onPause, cancelling cameraCenterTimer");
            cameraCenterTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshSharedPreferences(true);

        Log.d(Constants.NAVIGATION_APP, "onResume, creating new mapPollTimer");
        mapPollTimer = new Timer();

        Log.d(Constants.NAVIGATION_APP, "onResume, scheduling mapPollTimer at fixed rate");
        mapPollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d(Constants.NAVIGATION_APP, "Map Poll Timer: Requesting map poll");
                requestOccupancyEstimate(MAP_POLL_MODE);
            }
        }, mMapPollInterval, mMapPollInterval);

        Log.d(Constants.NAVIGATION_APP, "onResume, creating new cameraCenterTimer");
        cameraCenterTimer = new Timer();

        Log.d(Constants.NAVIGATION_APP, "onResume, scheduling cameraCenterTimer at fixed rate");
        cameraCenterTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ActivityNavigation.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(Constants.NAVIGATION_APP, "Camera Center Timer: Setting last_camera_center on UI Thread");
                        last_camera_center = mMap.getCameraPosition().target;
                    }
                });
            }
        }, mCameraCenterInterval, mCameraCenterInterval);
    }

    @Override
    public void onStop() {
        Log.d(Constants.NAVIGATION_APP, "onStop");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, ActivityConfigure.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_api_test) {
            startActivity(new Intent(this, ActivityAPITest.class));
        } else if (id == R.id.nav_sent_log) {
            startActivity(new Intent(this, ActivitySentLog.class));
        } else if (id == R.id.nav_crowd_observation) {
            requestOccupancyEstimate(CROWD_OBSERVATION_MODE);
        } else if (id == R.id.nav_likely_place) {
            findLikelyPlaces();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    Marker last_user_marker;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));

            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
            }
        }

        enableMapMyLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(last_user_marker != null) {
                    last_user_marker.remove();
                }
                last_user_marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Destination").snippet("Occupancy Loading.."));
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                if(!occupancy_square_id_to_occupancy.containsKey(polygon.getId())) {
                    Toast.makeText(ActivityNavigation.this, "This is a debug square, has no occupancy", Toast.LENGTH_SHORT).show();
                    return;
                }

                Float occupancy = occupancy_square_id_to_occupancy.get(polygon.getId());

                Toast.makeText(ActivityNavigation.this, "Occupancy " + occupancy, Toast.LENGTH_SHORT).show();
            }
        });
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

    public void findLikelyPlaces() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ActivityNavigation.this, "Cannot findLikelyPlaces, no ACCESS_FINE_LOCATION permission", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mGoogleApiClient == null) {
            Toast.makeText(ActivityNavigation.this, "Cannot find likely places, mGoogleApiClient is null", Toast.LENGTH_SHORT).show();
            Log.d(Constants.GOOGLE_API_CLIENT, "Cannot find likely places, mGoogleApiClient is null");
            return;
        }

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);

        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Toast.makeText(ActivityNavigation.this, "Place Name: " + placeLikelihood.getPlace().getName(), Toast.LENGTH_SHORT).show();
                    break;
                }

                if(likelyPlaces.getCount() == 0) {
                    Toast.makeText(ActivityNavigation.this, "Sorry, I could not identify the name of this place!", Toast.LENGTH_SHORT).show();
                }

                likelyPlaces.release();
            }
        });
    }

    public static final String CROWD_OBSERVATION_MODE = "CROWD_OBSERVATION_MODE";
    public static final String MAP_POLL_MODE = "MAP_POLL_MODE";

    public void requestOccupancyEstimate(String mode) {
        /* Only Toast for CROWD OBSERVATION MODE, as MAP POLL MODE on another thread */

//        if(serviceDataCollection == null) {
//            if(mode.equals(CROWD_OBSERVATION_MODE)) Toast.makeText(ActivityNavigation.this, "Data Collection Service has not started yet, please wait", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Uri uri;
        ContentValues contentValues;

        switch(mode) {
            case ActivityNavigation.MAP_POLL_MODE:
                uri = Uri.parse( NavigationContentProvider.CONTENT_URI + "/OCCUPANCY_ESTIMATE_BULK");

                /* TODO: Concurrency issue?? */
                if(last_camera_center == null) {
                    Log.d(Constants.NAVIGATION_APP, "Checked for last_camera_center, null, cannot map poll");
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                int index_count = 0;

                double lat = last_camera_center.latitude;
                double lng = last_camera_center.longitude;

                for(double lat_offset = -lat_target_offset; lat_offset < lat_target_offset + lat_increment; lat_offset += lat_increment) {
                    for(double lng_offset = -lng_target_offset; lng_offset < lng_target_offset + lat_increment; lng_offset += lng_increment) {
                        try {
                            double this_lat = lat + lat_offset;
                            this_lat -= this_lat % lat_increment; //align to fixed grid (wont move around as map does)

                            double this_lng = lng + lng_offset;
                            this_lng -= this_lng % lng_increment; //align to fixed grid (wont move around as map does)

                            jsonObject.put(
                                    String.valueOf( index_count++ ),
                                    new JSONObject().put("lat", this_lat).put("lng", this_lng)
                            );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                contentValues = new ContentValues();
                contentValues.put(NavigationContract.OccupancyEstimateBulk.ARG_LAT_LNG_LIST, jsonObject.toString());
                getContentResolver().insert(uri, contentValues);
                break;
            case ActivityNavigation.CROWD_OBSERVATION_MODE:
                uri = Uri.parse( NavigationContentProvider.CONTENT_URI + "/OCCUPANCY_ESTIMATE");

                Location lastLocation = serviceDataCollection.getLastLocation();

                if(lastLocation == null) {
                    if(mode.equals(CROWD_OBSERVATION_MODE)) Toast.makeText(ActivityNavigation.this, "App has not received a location update yet, please wait", Toast.LENGTH_SHORT).show();
                    return;
                }

                contentValues = new ContentValues();
                contentValues.put("lat", lastLocation.getLatitude());
                contentValues.put("lng", lastLocation.getLongitude());
                getContentResolver().insert(uri, contentValues);
                break;
        }


        /* Broadcast Receiver calls showCrowdObservationsAlertDialog if server returns occupancy estimate */
    }

    private void showCrowdObservationAlertDialog(String occupancy_estimate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crowd Observation");

        builder.setTitle("Occupancy Estimation");

        if(occupancy_estimate.equals("null")) {
            builder.setMessage("We have no data for your location, please enter your estimate");
        } else {
            builder.setMessage("We predict " + occupancy_estimate + " people are at your current location, please enter your estimate");
        }

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mString = input.getText().toString();
                Integer user_estimate;

                try {
                    user_estimate = Integer.parseInt(mString);
                } catch(NumberFormatException ex) {
                    Toast.makeText(ActivityNavigation.this, "Error sending your estimate, not a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(serviceDataCollection != null && serviceDataCollection.sendCrowdObservation(user_estimate)) {
                    Toast.makeText(ActivityNavigation.this, "Thank you for your estimate of " + user_estimate + " we will use this to make our service better!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityNavigation.this, "Error sending your estimate, Data Collection Service is not initialized", Toast.LENGTH_SHORT).show();
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

        if(serviceDataCollection != null) {
            foreground_service = serviceDataCollection.isForegroundNotification();

            unbindService(locationServiceConnection);
        }

        if(!foreground_service) {
            stopService(new Intent(this, ServiceDataCollection.class));
        }

        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        unregisterReceiver(occupancyEstimateReceiver);
    }

    /* Shows and hides the location text prompt in the Main Activity
     * Based on whether the permission have been granted or not to access location */
    private void checkPermissionsStartService(boolean requestIfNotGranted) {

        if(PermissionManager.checkAllPermissions(this, requestIfNotGranted) && serviceDataCollection == null) {
            Log.d(Constants.PERMISSIONS, "All permissions granted: Launching serviceDataCollection");

            enableMapMyLocation();

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

        if (grantResults.length > 0 && PermissionManager.isManagedPermission(requestCode)) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsStartService(true);
            } else {
                /* Do not keep asking user for that permission if they denied the first time */
                checkPermissionsStartService(false);
            }
        } else {
            Toast.makeText(ActivityNavigation.this, "onRequestPermissionResult case not set for requestCode " + requestCode, Toast.LENGTH_LONG).show();
        }

    }



}
