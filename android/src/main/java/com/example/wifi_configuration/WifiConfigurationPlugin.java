package com.example.wifi_configuration;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.example.wifi_configuration.connect.ConnectionSuccessListener;
import com.example.wifi_configuration.connect.WifiCallback;
import com.example.wifi_configuration.manager.WifiUtils;
import com.example.wifi_configuration.util.Constant;

import static com.example.wifi_configuration.PermissionHelper.FINE_LOCATION_PERMISSION;

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
                if (id == FINE_LOCATION_PERMISSION) {
                            if (grantResults.length > 0) {
                                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
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
        requestLocationPermission();
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
                            result.success(WifiStatus.notConnected.name());
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
        return null;
    }



    private void requestLocationPermission() {

        String []permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!PermissionHelper.checkFineLocationPermission(Constant.activity)) {
            ActivityCompat.requestPermissions(registrar.activity(), permissions, FINE_LOCATION_PERMISSION);
        } else {
            getWifiData();
        }
    }


    public static void locationPermissionCallbck(boolean success) {
        if ( success) {
            getWifiData();
        } else {
            Log.e("Permissions", WifiStatus.locationNotAllowed.name());
            Constant.result.success(WifiStatus.locationNotAllowed.name());
        }
    }

    private static void getWifiData(){
        if (Constant.methodCalled.method.equals("connectToWifi")) {
            connectWithWPA(Constant.methodCalled.argument("ssid"), Constant.methodCalled.argument("password"), Constant.context, Constant.result);

        } else if (Constant.methodCalled.method.equals("getWifiList")) {
            Constant.result.success(getAvailableWifiList());

        } else if (Constant.methodCalled.method.equals("isConnectedToWifi")) {
            Constant.result.success(isWifiConnected(Constant.methodCalled.argument("ssid")));

        }
    }



    public static boolean isWifiConnected(final String wifiSsid) {

        WifiManager wifiManager = (WifiManager) Constant.context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        String wifiConnected = info.getSSID();
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

}
