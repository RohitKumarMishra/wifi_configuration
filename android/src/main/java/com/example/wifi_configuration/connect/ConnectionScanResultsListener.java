package com.example.wifi_configuration.connect;


import android.net.wifi.ScanResult;


import java.util.List;


public interface ConnectionScanResultsListener
{
    ScanResult onConnectWithScanResult( List<ScanResult> scanResults);
}
