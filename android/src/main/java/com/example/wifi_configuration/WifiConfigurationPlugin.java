package com.example.wifi_configuration;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.example.wifi_configuration.connect.ConnectionSuccessListener;
import com.example.wifi_configuration.manager.WifiUtils;
import com.example.wifi_configuration.util.Constant;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/**
 *
 */

enum WifiStatus {
    connected("connected"),
    notConnected("notConnected"),
    locationNotAllowed("locationNotAllowed");

    private String status;
    private WifiStatus(String status){
        this.status = status;
    }
}

/** WifiConfigurationPlugin */
public class WifiConfigurationPlugin implements MethodCallHandler {


    private static WifiUtils wifiUtils;
    private static Registrar registrar;
    private static boolean isLocationPermissionAllowed = false;

    /** Plugin registration. */
    public static void registerWith(Registrar registrar) {
        WifiConfigurationPlugin.registrar =  registrar;
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "wifi_configuration");
        Constant.context = registrar.context();
        Constant.activity = registrar.activity();

        channel.setMethodCallHandler(new WifiConfigurationPlugin());
        registrar.addRequestPermissionsResultListener(new PluginRegistry.RequestPermissionsResultListener() {
            @Override
            public boolean onRequestPermissionsResult(int id, String[] permissions, int[] grantResults) {
                if (id == PermissionHelper.FINE_LOCATION_PERMISSION) {
                    if (grantResults.length > 0) {
                        boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        isLocationPermissionAllowed = locationAccepted;
                        locationPermissionCallbck(locationAccepted);


                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
        wifiUtils = new WifiUtils(Constant.context);

    }

    /**
     *
     * @param call
     * @param result
     */
    @Override
    public void onMethodCall(MethodCall call, Result result) {

        /**
         *
         */
        Constant.result = result;
        Constant.methodCalled = call;
        if (Constant.methodCalled.method.equals("connectToWifi")) {
            requestLocationPermission();
        } else if (Constant.methodCalled.method.equals("getWifiList")) {
            Constant.result.success(getAvailableWifiList());

        } else if (Constant.methodCalled.method.equals("isConnectedToWifi")) {
            Constant.result.success(isWifiConnected(Constant.methodCalled.argument("ssid")));
        } else if (Constant.methodCalled.method.equals("connectedToWifi")) {
            requestLocationPermissionForConnectedWifiName();

        }
    }



    /**
     * This method is used to connect to specific ssid
     *
     * @param ssid
     * @param password
     * @param context
     * @param result
     */
    private static void connectWithWPA(String ssid, String password, Context context, Result result) {

        wifiUtils.withContext(context)
                .connectWith(ssid +
                        "", password)
                .setTimeout(40000)
                .onConnectionResult(new ConnectionSuccessListener() {
                    @Override
                    public void isSuccessful(boolean isSuccess) {
                        if (isSuccess) {
                            result.success(WifiStatus.connected.name());
                        } else {
                            openWifiSetting();
                            // result.success(WifiStatus.notConnected.name());
                        }


                    }
                })
                .start();

    }


    /**
     *
     * @return
     */
    private  static List<String> getAvailableWifiList() {
        List<String> wifiList = new ArrayList<String>();
        if (wifiUtils.getScanWifiResult() != null){
            Log.d("WifiResults-->", wifiUtils.getScanWifiResult()+"");

            for (ScanResult wifiName : wifiUtils.getScanWifiResult()
            ) {
                Log.d("WifiUtils", wifiName.SSID);
                wifiList.add(wifiName.SSID);
            }
            return wifiList;
        }
        return wifiList;
    }



    private void requestLocationPermission() {

        String []permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!PermissionHelper.checkFineLocationPermission(Constant.activity)) {
            ActivityCompat.requestPermissions(registrar.activity(), permissions, PermissionHelper.FINE_LOCATION_PERMISSION);
        } else {
            isLocationPermissionAllowed = true;
            getWifiData(true);
        }
    }


    private void requestLocationPermissionForConnectedWifiName() {

        String []permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!PermissionHelper.checkFineLocationPermission(Constant.activity)) {
            ActivityCompat.requestPermissions(registrar.activity(), permissions, PermissionHelper.FINE_LOCATION_PERMISSION);

        } else {
            isLocationPermissionAllowed = true;
            Constant.result.success(this.connectedToWifi());
        }
    }


    public static void locationPermissionCallbck(boolean success) {
        getWifiData(success);
    }

    private static void getWifiData(boolean success){
        if (Constant.methodCalled.method.equals("connectToWifi")) {
            if (isLocationPermissionAllowed)
                connectWithWPA(Constant.methodCalled.argument("ssid"), Constant.methodCalled.argument("password"),Constant.context, Constant.result);
            else {
                openAppSettings();
            }
        } else if (Constant.methodCalled.method.equals("connectToWifi")) {
            if (isLocationPermissionAllowed)
                connectedToWifi();
            else {
                openAppSettings();
            }
        }else if (Constant.methodCalled.method.equals("connectedToWifi")) {
            if (success) {
                Constant.result.success(connectedToWifi());
            } else {
                Constant.result.success("Please allow location to get wifi name");
            }

        }

    }



    public static boolean isWifiConnected(final String wifiSsid) {

        WifiManager wifiManager = (WifiManager) Constant.context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        String wifiConnected = info.getSSID();
        Log.d("Wifi ID", wifiSsid + "   " + wifiConnected);


        if (wifiConnected.length() > 2) {
            wifiConnected = wifiConnected.replace("\"", "");
        }
        boolean isWifiConnect = false;
        if (wifiSsid != null) {
            if (wifiConnected.equals(wifiSsid)) {
                isWifiConnect = true;
            }
        }
        return isWifiConnect;
    }

    private static void openAppSettings(){

        showAletDialog();

    }

    private static void showAletDialog(){

        final Dialog dialog = new Dialog(Constant.activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_ok);

        TextView txtOk = (TextView) dialog.findViewById(R.id.btn_ok);

        txtOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", Constant.methodCalled.argument("packageName"), null);
                intent.setData(uri);
                Constant.activity.startActivity(intent);
                Constant.result.success(WifiStatus.locationNotAllowed.name());
            }
        });

        dialog.show();

    }


    public static void openWifiSetting() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Constant.activity.startActivity(intent);
            Constant.result.success(WifiStatus.notConnected.name());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String connectedToWifi() {

        WifiManager wifiManager = (WifiManager) Constant.context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        String connectedWifi = info.getSSID();

        if (connectedWifi.length() > 2 ) {
            if (connectedWifi == "<unknown ssid>" || connectedWifi.contains("<")) {
                return "";
            } else {
                connectedWifi = connectedWifi.replace("\"", "");
                return (connectedWifi);
            }

        } else {
            return ("");
        }

    }


}
