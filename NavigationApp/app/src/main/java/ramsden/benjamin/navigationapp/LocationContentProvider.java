package ramsden.benjamin.navigationapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

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
    public static final String BASE_PATH = NavigationServerContract.LocationLogEntry.TABLE_NAME;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);


    /* Defines the different types of URI that are valid to be called
     * On this content provider */
    private static final int ALL = 50;
    private static final int AFTER_DATE = 51;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, ALL);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/after_date/*", AFTER_DATE);
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

        /* TODO: Implement RESTFUL Interface, using Service Helper -> Service -> RESTFUL */

        return null;
    }

    /* Provides the interface for activities to delete stored location and time information
     * From the SQLite Database */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d("g53mdp","LocationContentProvider called with delete");

        /* TODO: Implement RESTFUL Interface, using Service Helper -> Service -> RESTFUL */

        return 0;
    }

    /* Provides the interface for activities to update stored location and time information
     * From the SQLite Database */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d("g53mdp","LocationContentProvider called with update");

        /* TODO: Implement RESTFUL Interface, using Service Helper -> Service -> RESTFUL */

        return 0;
    }
}

