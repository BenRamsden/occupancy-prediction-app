package ramsden.benjamin.navigationapp;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * This class checks for and acquires permissions on the behalf of an activity
 * This abstracts away the lengthy permission code from the Activity UI code
 * And provides a single point of failure to analyse if the permissions stop working
 */

public class PermissionManager {

    public static boolean checkAllPermissions(Activity activity, boolean requestIfNotGranted) {

        if(! requestPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION, Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION, requestIfNotGranted)) {
            return false;
        };

        if(! requestPermission(activity, android.Manifest.permission.RECORD_AUDIO, Constants.MY_PERMISSIONS_REQUEST_RECORD_AUDIO, requestIfNotGranted)) {
            return false;
        };

        if(! requestPermission(activity, android.Manifest.permission.BLUETOOTH, Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH, requestIfNotGranted)) {
            return false;
        };

        if(! requestPermission(activity, android.Manifest.permission.BLUETOOTH_ADMIN, Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN, requestIfNotGranted)) {
            return false;
        };

        if(! requestPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE, requestIfNotGranted)) {
            return false;
        };

        if(! requestPermission(activity, android.Manifest.permission.ACCESS_WIFI_STATE, Constants.MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE, requestIfNotGranted)) {
            return false;
        };

        if(! requestPermission(activity, Manifest.permission.CHANGE_WIFI_STATE, Constants.MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE, requestIfNotGranted)) {
            return false;
        };

        return true;
    }

    public static boolean isManagedPermission(int requestCode) {

        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_FINE_LOCATION:
            case Constants.MY_PERMISSIONS_REQUEST_RECORD_AUDIO:
            case Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH:
            case Constants.MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN:
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            case Constants.MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE:
            case Constants.MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE:
                return true;
            default:
                return false;
        }

    }

    private static boolean requestPermission(Activity activity, String permission, int requestCode, boolean requestIfNotGranted) {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(Constants.PERMISSIONS, "Got " + permission + " permission");
            return true;
        } else if(requestIfNotGranted) {
            Log.d(Constants.PERMISSIONS, "Requesting " + permission + " permission");

            ActivityCompat.requestPermissions(activity, new String[]{ permission }, Constants.MY_PERMISSIONS_REQUEST_CHANGE_WIFI_STATE);
            return false;
        } else {
            Log.d(Constants.PERMISSIONS, "User denied " + permission + " permission");

            Toast.makeText(activity, "This application cannot function without the " + permission + " permission, restart the app if you change your mind", Toast.LENGTH_LONG).show();
            return false;
        }

    }

}
