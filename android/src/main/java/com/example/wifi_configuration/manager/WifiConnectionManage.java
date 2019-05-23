package com.example.wifi_configuration.manager;


import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 10/8/18.
 */

public class WifiConnectionManage {

    private Context context;
    private String TAG = WifiConnectionManage.class.getName();

    public WifiConnectionManage(final Context context) {
        this.context = context;
    }

    public boolean isWifiConnected(final String wifiSsid) {

        WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        String wifiConnected = info.getSSID();
        if (wifiConnected.length() > 2) {
            wifiConnected = wifiConnected.replace("\"", "");
        }
        boolean isWifiConnect = false;
        if (wifiSsid != null) {
         //   SessionForToken.putHotelWifiName(context, wifiSsid);
            if (wifiConnected.equals(wifiSsid)) {
                isWifiConnect = true;
            }
        }
        return isWifiConnect;
    }


    public void getConfiguredWifiList(final Activity activity,
                                      String ssid, String passowrd) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled() == false) {
                wifi.setWifiEnabled(true);
                getConnectedList(wifiManager, activity, ssid, passowrd);
            } else {
                getConnectedList(wifiManager, activity, ssid, passowrd);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getConnectedList(final WifiManager wifiManager, final Activity activity, final String ssid,
                                  final String passowrd) {
        ArrayList<ScanResult> list = (ArrayList<ScanResult>) wifiManager.getScanResults();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiConnected = "'" + wifiInfo.getSSID() + "'";
     //   Utility.log(TAG, "wifiConnected wifi " + wifiConnected);
        if (!wifiConnected.equals(ssid)) {
            for (ScanResult i : list) {
                if (i.SSID != null) {
                    if (i.SSID.equalsIgnoreCase(ssid)) {
                        connectWiFi(activity, ssid, i, context, passowrd);
                    }
                }
            }
        } else {
        }
    }

    private void connectWiFi(final Activity activity,
                             final String ssid, ScanResult scanResult, final Context context, String password) {
        try {
            String networkSSID = scanResult.SSID;
            String networkPass = password;
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.priority = 99999;
            if (scanResult.capabilities.toUpperCase().contains("WEP")) {
             //   Utility.log("rht", "Configuring WEP");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                if (networkPass.matches("^[0-9a-fA-F]+$")) {
                    conf.wepKeys[0] = networkPass;
                } else {
                    conf.wepKeys[0] = "\"".concat(networkPass).concat("\"");
                }

                conf.wepTxKeyIndex = 0;

            } else if (scanResult.capabilities.toUpperCase().contains("WPA")) {

                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                conf.preSharedKey = "\"" + networkPass + "\"";

            } else {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }


            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int networkId = wifiManager.addNetwork(conf);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
