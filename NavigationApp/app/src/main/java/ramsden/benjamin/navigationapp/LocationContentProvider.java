package ramsden.benjamin.navigationapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.URISyntaxException;

/**
 * Created by ben on 01/02/2017.
 */

/**
 * This class provides the access point to the applications activities
 * To the location log database, where the users location data collected by
 * The app is stored for processing
 */
public class LocationContentProvider extends ContentProvider {
    public static final String AUTHORITY = "ramsden.benjamin.navigationapp.LocationContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");


    /* Defines the different types of URI that are valid to be called
     * On this content provider */
    private static final int USERS = 51;
    private static final int HOTSPOTS = 52;
    private static final int HOTSPOT_OBSERVATIONS = 53;
    private static final int AUDIO_OBSERVATIONS = 54;
    private static final int CROWD_OBSERVATIONS = 55;
    private static final int BLUETOOTH_OBSERVATIONS = 56;
    private static final int ACCELEROMETER_OBSERVATIONS = 57;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(AUTHORITY, NavigationServerContract.Users.TABLE_NAME, USERS);
        URI_MATCHER.addURI(AUTHORITY, NavigationServerContract.Hotspots.TABLE_NAME, HOTSPOTS);
        URI_MATCHER.addURI(AUTHORITY, NavigationServerContract.HotspotObservations.TABLE_NAME, HOTSPOT_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationServerContract.AudioObservations.TABLE_NAME, AUDIO_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationServerContract.CrowdObservations.TABLE_NAME, CROWD_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationServerContract.BluetoothObservations.TABLE_NAME, BLUETOOTH_OBSERVATIONS);
        URI_MATCHER.addURI(AUTHORITY, NavigationServerContract.AccelerometerObservations.TABLE_NAME, ACCELEROMETER_OBSERVATIONS);
    }

    @Override
    public boolean onCreate() {
        Log.d("g53mdp", "LocationContentProvider onCreate");
        return true;
    }

    /* Provides the interface for activities to request stored location and time information
     * From the SQLite Database */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("g53mdp","LocationContentProvider called with query");
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
    public Uri insert(Uri uri, ContentValues values) {
        Log.d("g53mdp","LocationContentProvider called with insert");

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

        return null;
    }

    /* Provides the interface for activities to delete stored location and time information
     * From the SQLite Database */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d("g53mdp","LocationContentProvider called with delete");
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
        Log.d("g53mdp","LocationContentProvider called with update");
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

