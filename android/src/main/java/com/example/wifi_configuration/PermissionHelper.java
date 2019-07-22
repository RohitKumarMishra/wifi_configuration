package com.example.wifi_configuration;

/**
 * Created by rahul on 18/10/18.
 */


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public final class PermissionHelper {

    private static final String MNC = "MNC";
    public static final int CAMERA_PERMISSION = 1000;
    public static final int STORAGE_PERMISSION = 1001;
    public static final int CALL_PERMISSION = 1002;
    public static final int FINE_LOCATION_PERMISSION = 1003;
    public static final int FINE_WIFI_LOCATION_PERMISSION = 1004;

    // Camera group.
    public static final String CAMERA = Manifest.permission.CAMERA;

    // Location group.
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    // Storage group.
    public static final String READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;

    public static boolean checkCameraPermission(final Context context) {
        int cameraPermission = ContextCompat.checkSelfPermission(context, CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(context, WRITE_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPhonePermission(final Context context) {
        int callPermission = ContextCompat.checkSelfPermission(context, CALL_PHONE);
        return callPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkFineLocationPermission(final Context context) {
        int finePermission = ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION);
        return finePermission == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean checkStoragePermission(final Context context) {
        int readStorageResult = ContextCompat.checkSelfPermission(context, READ_STORAGE);
        int writeStorageResult = ContextCompat.checkSelfPermission(context, WRITE_STORAGE);
        return readStorageResult == PackageManager.PERMISSION_GRANTED && writeStorageResult == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkCorseFinePermission(final Context context) {
        int corsePermissionResult = ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION);
        int finePermissionResult = ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION);
        return corsePermissionResult == PackageManager.PERMISSION_GRANTED && finePermissionResult == PackageManager.PERMISSION_GRANTED;
    }


    public static void requestPermission(final Activity activity, final int permissionRequestCode, String... args) {
        ActivityCompat.requestPermissions(activity, args, permissionRequestCode);
    }

    /**
     * open android settings screen for your app.
     */
    public static void openSettingsScreen(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + context.getPackageName());
        intent.setData(uri);
        context.startActivity(intent);
    }

}
