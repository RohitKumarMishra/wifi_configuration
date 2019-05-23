package com.example.wifi_configuration.scan;


import android.net.wifi.ScanResult;

import java.util.List;


public interface ScanResultsListener
{
    void onScanResults( List<ScanResult> scanResults);
}
