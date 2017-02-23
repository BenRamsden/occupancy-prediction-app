package ramsden.benjamin.navigationapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NoCache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ben on 01/02/2017.
 */

/**
 * This class provides the access point to the applications activities
 * To the location log database, where the users location data collected by
 * The app is stored for processing
 */
public class NavigationContentProvider extends ContentProvider {
    public static final String AUTHORITY = "ramsden.benjamin.navigationapp.NavigationContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private final String api_root = "http://54.154.109.216:80";

    /* TODO make dynamic */
    public String my_api_token = "koH6a1UC71rTDM1LKppXeKYJ54cjc8nIfuJAKPly1GDYpjMMLLCuK5LBp3fXAEkCcID1jCh5pCQp9D8DmCWhJHQlLcUcy4gD68Qy";

    private RequestQueue apiRequestQueue;

    private final Response.Listener<JSONObject> DEFAULT_RESPONSE_LISTENER = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            //Toast.makeText(getContext(), "NET_RESPONSE: "+response.toString(), Toast.LENGTH_SHORT).show();
            Log.d(Constants.CONTENT_PROVIDER, "RESPONSE: " + response);
        }
    };

    private final Response.ErrorListener DEFAULT_ERROR_LISTENER = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(Constants.CONTENT_PROVIDER, "ERROR: " + error);
            Toast.makeText(getContext(), "NET_ERROR: Printed to Stack Trace", Toast.LENGTH_SHORT).show();
            //error.printStackTrace(System.out);
        }
    };

    /* Defines the different types of URI that are valid to be called
     * On this content provider */
    private static final int USERS = 51;
    private static final int HOTSPOTS = 52;
    private static final int HOTSPOT_OBSERVATIONS = 53;
    private static final int AUDIO_OBSERVATIONS = 54;
    private static final int CROWD_OBSERVATIONS = 55;
    private static final int BLUETOOTH_OBSERVATIONS = 56;
    private static final int ACCELEROMETER_OBSERVATIONS = 57;
    private static final int OCCUPANCY_ESTIMATE = 58;
    private static final int OCCUPANCY_ESTIMATE_BULK = 59;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(AUTHORITY, NavigationContract.Users.TABLE_NAME, USERS);
        URI_MATCHER.addURI(AUTHORITY, NavigationContract.Hotspots.TABLE_NAME, HOTSPOTS);
        URI_MATCHER.addURI(AUTHORITY, NavigationContract.HotspotObservations.TABLE_NAME, HOTSPOT_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationContract.AudioObservations.TABLE_NAME, AUDIO_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationContract.CrowdObservations.TABLE_NAME, CROWD_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationContract.BluetoothObservations.TABLE_NAME, BLUETOOTH_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationContract.AccelerometerObservations.TABLE_NAME, ACCELEROMETER_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, "OCCUPANCY_ESTIMATE", OCCUPANCY_ESTIMATE);
        URI_MATCHER.addURI(AUTHORITY, "OCCUPANCY_ESTIMATE_BULK", OCCUPANCY_ESTIMATE_BULK);
    }

    @Override
    public boolean onCreate() {
        Log.d(Constants.NAVIGATION_APP, "NavigationContentProvider onCreate");

        Cache cache = new NoCache();
        Network network = new BasicNetwork(new HurlStack());
        apiRequestQueue = new RequestQueue(cache, network);
        apiRequestQueue.start();

        return true;
    }

    /* Provides the interface for activities to request stored location and time information
     * From the SQLite Database */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(Constants.NAVIGATION_APP,"NavigationContentProvider called with query");

        int uriType = URI_MATCHER.match(uri);

        String api_sub = null;

        switch (uriType) {
            case USERS:
                api_sub = "/users";
                break;
            case HOTSPOTS:
                api_sub = "/hotspots";
                break;
            case HOTSPOT_OBSERVATIONS:
                api_sub = "/observations/hotspot";
                break;
            case AUDIO_OBSERVATIONS:
                api_sub = "/observations/audio";
                break;
            case CROWD_OBSERVATIONS:
                api_sub = "/observations/crowd";
                break;
            case BLUETOOTH_OBSERVATIONS:
                api_sub = "/observations/bluetooth";
                break;
            case ACCELEROMETER_OBSERVATIONS:
                api_sub = "/observations/accelerometer";
                break;
            default:
                throw new Error("Query: Couldn't match URI: " + uri);
        }

        if(api_sub == null) {
            Toast.makeText(getContext(), "ContentProvider query: api_sub string not set, cannot send", Toast.LENGTH_SHORT).show();
            return null;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                api_root+api_sub+"?apitoken="+my_api_token,
                null,
                DEFAULT_RESPONSE_LISTENER,
                DEFAULT_ERROR_LISTENER
        );

        apiRequestQueue.add(jsonRequest);

        return null;

    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    /* Provides the interface for activities to insert location and time information
     * into the SQLite Database */
    @Nullable
    @Override
    public Uri insert(Uri uri, final ContentValues values) {
        Log.d(Constants.NAVIGATION_APP,"NavigationContentProvider called with insert");

        int uriType = URI_MATCHER.match(uri);

        String api_sub = null;
        Response.Listener<JSONObject> responseListener = DEFAULT_RESPONSE_LISTENER;

        final JSONObject insertJSON = new JSONObject();

        try {
            switch (uriType) {
                case USERS:
                    api_sub = "/users";
                    insertJSON.put(NavigationContract.Users.KEY_USERNAME, values.get(NavigationContract.Users.KEY_USERNAME));
                    insertJSON.put(NavigationContract.Users.KEY_PASSWORD, values.get(NavigationContract.Users.KEY_PASSWORD));
                    insertJSON.put(NavigationContract.Users.KEY_FULL_NAME, values.get(NavigationContract.Users.KEY_FULL_NAME));
                    insertJSON.put(NavigationContract.Users.KEY_API_TOKEN, values.get(NavigationContract.Users.KEY_API_TOKEN));
                    insertJSON.put(NavigationContract.Users.KEY_EMAIL_ADDRESS, values.get(NavigationContract.Users.KEY_EMAIL_ADDRESS));
                    insertJSON.put(NavigationContract.Users.KEY_REGISTER_DATE, values.get(NavigationContract.Users.KEY_REGISTER_DATE));
                    break;
                case HOTSPOTS:
                    api_sub = "/hotspots";
                    throw new Error("/hotspots does not have a direct insert api, use /observations/hotspot");
                case HOTSPOT_OBSERVATIONS:
                    api_sub = "/observations/hotspot";
                    /* Hotspots entry */
                    insertJSON.put(NavigationContract.Hotspots.KEY_SSID, values.get(NavigationContract.Hotspots.KEY_SSID));
                    insertJSON.put(NavigationContract.Hotspots.KEY_FREQUENCY, values.get(NavigationContract.Hotspots.KEY_FREQUENCY));
                    insertJSON.put(NavigationContract.Hotspots.KEY_MAC, values.get(NavigationContract.Hotspots.KEY_MAC));

                    /* Hotspot Observation Entry */
                    insertJSON.put(NavigationContract.HotspotObservations.KEY_LATITUDE, values.get(NavigationContract.HotspotObservations.KEY_LATITUDE));
                    insertJSON.put(NavigationContract.HotspotObservations.KEY_LONGITUDE, values.get(NavigationContract.HotspotObservations.KEY_LONGITUDE));
                    insertJSON.put(NavigationContract.HotspotObservations.KEY_SIGNAL_LEVEL, values.get(NavigationContract.HotspotObservations.KEY_SIGNAL_LEVEL));
                    insertJSON.put(NavigationContract.HotspotObservations.KEY_OBSERVATION_DATE, values.get(NavigationContract.HotspotObservations.KEY_OBSERVATION_DATE));
                    break;
                case AUDIO_OBSERVATIONS:
                    api_sub = "/observations/audio";
                    insertJSON.put(NavigationContract.AudioObservations.KEY_LATITUDE, values.get(NavigationContract.AudioObservations.KEY_LATITUDE));
                    insertJSON.put(NavigationContract.AudioObservations.KEY_LONGITUDE, values.get(NavigationContract.AudioObservations.KEY_LONGITUDE));
                    insertJSON.put(NavigationContract.AudioObservations.KEY_AUDIO_HISTOGRAM, new JSONObject(values.getAsString(NavigationContract.AudioObservations.KEY_AUDIO_HISTOGRAM))); //parse back into json to prevent triple slashes
                    insertJSON.put(NavigationContract.AudioObservations.KEY_OBSERVATION_DATE, values.get(NavigationContract.AudioObservations.KEY_OBSERVATION_DATE));
                    break;
                case CROWD_OBSERVATIONS:
                    api_sub = "/observations/crowd";
                    insertJSON.put(NavigationContract.CrowdObservations.KEY_LATITUDE, values.get(NavigationContract.CrowdObservations.KEY_LATITUDE));
                    insertJSON.put(NavigationContract.CrowdObservations.KEY_LONGITUDE, values.get(NavigationContract.CrowdObservations.KEY_LONGITUDE));
                    insertJSON.put(NavigationContract.CrowdObservations.KEY_OCCUPANCY_ESTIMATE, values.get(NavigationContract.CrowdObservations.KEY_OCCUPANCY_ESTIMATE));
                    insertJSON.put(NavigationContract.CrowdObservations.KEY_OBSERVATION_DATE, values.get(NavigationContract.CrowdObservations.KEY_OBSERVATION_DATE));
                    break;
                case BLUETOOTH_OBSERVATIONS:
                    api_sub = "/observations/bluetooth";
                    insertJSON.put(NavigationContract.BluetoothObservations.KEY_LATITUDE, values.get(NavigationContract.BluetoothObservations.KEY_LATITUDE));
                    insertJSON.put(NavigationContract.BluetoothObservations.KEY_LONGITUDE, values.get(NavigationContract.BluetoothObservations.KEY_LONGITUDE));
                    insertJSON.put(NavigationContract.BluetoothObservations.KEY_BLUETOOTH_COUNT, values.get(NavigationContract.BluetoothObservations.KEY_BLUETOOTH_COUNT));
                    insertJSON.put(NavigationContract.BluetoothObservations.KEY_OBSERVATION_DATE, values.get(NavigationContract.BluetoothObservations.KEY_OBSERVATION_DATE));
                    break;
                case ACCELEROMETER_OBSERVATIONS:
                    api_sub = "/observations/accelerometer";
                    insertJSON.put(NavigationContract.AccelerometerObservations.KEY_LATITUDE, values.get(NavigationContract.AccelerometerObservations.KEY_LATITUDE));
                    insertJSON.put(NavigationContract.AccelerometerObservations.KEY_LONGITUDE, values.get(NavigationContract.AccelerometerObservations.KEY_LONGITUDE));
                    insertJSON.put(NavigationContract.AccelerometerObservations.KEY_ACCELERATION_TIMELINE, new JSONObject(values.getAsString(NavigationContract.AccelerometerObservations.KEY_ACCELERATION_TIMELINE)));  //parse back into json to prevent triple slashes
                    insertJSON.put(NavigationContract.AccelerometerObservations.KEY_OBSERVATION_DATE, values.get(NavigationContract.AccelerometerObservations.KEY_OBSERVATION_DATE));
                    break;
                case OCCUPANCY_ESTIMATE:
                    api_sub = "/occupancy";
                    insertJSON.put(NavigationContract.OccupancyEstimate.ARG_LAT, values.get(NavigationContract.OccupancyEstimate.ARG_LAT));
                    insertJSON.put(NavigationContract.OccupancyEstimate.ARG_LNG, values.get(NavigationContract.OccupancyEstimate.ARG_LNG));

                    responseListener = new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String occupancy_estimate;

                            try {
                                occupancy_estimate = response.getString(NavigationContract.OccupancyEstimate.RESPONSE_OCCUPANCY);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return;
                            }

                            Intent intent = new Intent(ActivityNavigation.OCCUPANCY_ESTIMATE_RECEIVER);
                            intent.putExtra(NavigationContract.OccupancyEstimate.EXTRA_OCCUPANCY_ESTIMATE,occupancy_estimate);
                            intent.putExtra(NavigationContract.OccupancyEstimate.EXTRA_MODE, ActivityNavigation.CROWD_OBSERVATION_MODE);
                            getContext().sendBroadcast(intent);
                        }
                    };
                    break;
                case OCCUPANCY_ESTIMATE_BULK:
                    api_sub = "/occupancy/bulk";
                    insertJSON.put(NavigationContract.OccupancyEstimateBulk.ARG_LAT_LNG_LIST, new JSONObject(values.getAsString(NavigationContract.OccupancyEstimateBulk.ARG_LAT_LNG_LIST)) );

                    responseListener = new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String lat_lng_occupancy_list;

                            try {
                                lat_lng_occupancy_list = response.getString(NavigationContract.OccupancyEstimateBulk.RESPONSE_LAT_LNG_OCCUPANCY_LIST);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return;
                            }

                            Intent intent = new Intent(ActivityNavigation.OCCUPANCY_ESTIMATE_RECEIVER);
                            intent.putExtra(NavigationContract.OccupancyEstimateBulk.EXTRA_LAT_LNG_OCCUPANCY_LIST, lat_lng_occupancy_list);
                            intent.putExtra(NavigationContract.OccupancyEstimateBulk.EXTRA_MODE, ActivityNavigation.MAP_POLL_MODE);
                            getContext().sendBroadcast(intent);
                        }
                    };
                    break;
                default:
                    throw new Error("Query: Couldn't match URI: " + uri);
            }

        } catch(JSONException ex) {
            //Toast.makeText(getContext(), "JSON_EXCEPTION: Printed to Stack Trace", Toast.LENGTH_SHORT).show();
            ex.printStackTrace(System.out);
        }

        if(api_sub == null) {
            //Toast.makeText(getContext(), "ContentProvider insert: api_sub string not set, cannot send", Toast.LENGTH_SHORT).show();
            return null;
        }

        Log.d(Constants.CONTENT_PROVIDER, "POST to " + api_sub + ": " + insertJSON.toString());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.POST,
                api_root+api_sub+"?apitoken="+my_api_token,
                insertJSON,
                responseListener,
                DEFAULT_ERROR_LISTENER
        );

        apiRequestQueue.add(jsonRequest);

        Intent sent_log_intent = new Intent(ActivitySentLog.SENT_LOG_RECEIVER);
        sent_log_intent.putExtra(ActivitySentLog.SENT_EXTRA_FIELD, "To: " + api_sub + "\nBody: " + insertJSON.toString());
        getContext().sendBroadcast(sent_log_intent);
        Log.d(Constants.CONTENT_PROVIDER, "Sent sent_log_intent");

        return null;
    }

    /* Provides the interface for activities to delete stored location and time information
     * From the SQLite Database */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(Constants.NAVIGATION_APP,"NavigationContentProvider called with delete");
        /* TODO: Implement RESTFUL Interface, using Service Helper -> Service -> RESTFUL */

        int uriType = URI_MATCHER.match(uri);

        switch (uriType) {
            case USERS:
                break;
            case HOTSPOTS:
                break;
            case HOTSPOT_OBSERVATIONS:
                break;
            case AUDIO_OBSERVATIONS:
                break;
            case CROWD_OBSERVATIONS:
                break;
            case BLUETOOTH_OBSERVATIONS:
                break;
            case ACCELEROMETER_OBSERVATIONS:
                break;
            default:
                throw new Error("Query: Couldn't match URI: " + uri);
        }

        return 0;
    }

    /* Provides the interface for activities to update stored location and time information
     * From the SQLite Database */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(Constants.NAVIGATION_APP,"NavigationContentProvider called with update");
        /* TODO: Implement RESTFUL Interface, using Service Helper -> Service -> RESTFUL */

        int uriType = URI_MATCHER.match(uri);

        switch (uriType) {
            case USERS:
                break;
            case HOTSPOTS:
                break;
            case HOTSPOT_OBSERVATIONS:
                break;
            case AUDIO_OBSERVATIONS:
                break;
            case CROWD_OBSERVATIONS:
                break;
            case BLUETOOTH_OBSERVATIONS:
                break;
            case ACCELEROMETER_OBSERVATIONS:
                break;
            default:
                throw new Error("Query: Couldn't match URI: " + uri);
        }

        return 0;
    }
}

