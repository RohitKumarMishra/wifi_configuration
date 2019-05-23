package com.example.wifi_configuration.manager;

import android.os.Build;

import com.example.wifi_configuration.ConnectionWpsListener;
import com.example.wifi_configuration.connect.ConnectionScanResultsListener;
import com.example.wifi_configuration.connect.ConnectionSuccessListener;
import com.example.wifi_configuration.scan.ScanResultsListener;
import com.example.wifi_configuration.state.WifiStateListener;


public interface WifiConnectorBuilder {
    void start();

    interface WifiUtilsBuilder {
        void enableWifi(WifiStateListener wifiStateListener);

        void enableWifi();

        void disableWifi();

        
        WifiConnectorBuilder scanWifi( ScanResultsListener scanResultsListener);

        
        WifiSuccessListener connectWith( String ssid,  String password);

        
        WifiSuccessListener connectWith( String ssid,  String bssid,  String password);

        
        WifiSuccessListener connectWithScanResult( String password,  ConnectionScanResultsListener connectionScanResultsListener);

        
        WifiWpsSuccessListener connectWithWps( String bssid,  String password);

        void cancelAutoConnect();
    }

    interface WifiSuccessListener {
        
        WifiSuccessListener setTimeout(long timeOutMillis);

        
        WifiConnectorBuilder onConnectionResult( ConnectionSuccessListener successListener);
    }

    interface WifiWpsSuccessListener {
        
        WifiWpsSuccessListener setWpsTimeout(long timeOutMillis);
        
        WifiConnectorBuilder onConnectionWpsResult( ConnectionWpsListener successListener);
    }
}
