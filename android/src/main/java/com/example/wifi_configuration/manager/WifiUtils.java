package com.example.wifi_configuration.manager;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import android.util.Log;

import com.example.wifi_configuration.ConnectionWpsListener;
import com.example.wifi_configuration.connect.ConnectionScanResultsListener;
import com.example.wifi_configuration.connect.ConnectionSuccessListener;
import com.example.wifi_configuration.connect.WifiConnectionCallback;
import com.example.wifi_configuration.connect.WifiConnectionReceiver;
import com.example.wifi_configuration.scan.ScanResultsListener;
import com.example.wifi_configuration.scan.WifiScanCallback;
import com.example.wifi_configuration.scan.WifiScanReceiver;
import com.example.wifi_configuration.state.WifiStateCallback;
import com.example.wifi_configuration.state.WifiStateListener;
import com.example.wifi_configuration.state.WifiStateReceiver;

import java.util.ArrayList;
import java.util.List;

import static com.example.wifi_configuration.manager.ConnectorUtils.cleanPreviousConfiguration;
import static com.example.wifi_configuration.manager.ConnectorUtils.connectToWifi;
import static com.example.wifi_configuration.manager.ConnectorUtils.connectWps;
import static com.example.wifi_configuration.manager.ConnectorUtils.matchScanResult;
import static com.example.wifi_configuration.manager.ConnectorUtils.matchScanResultBssid;
import static com.example.wifi_configuration.manager.ConnectorUtils.matchScanResultSsid;
import static com.example.wifi_configuration.manager.ConnectorUtils.reenableAllHotspots;
import static com.example.wifi_configuration.manager.ConnectorUtils.registerReceiver;
import static com.example.wifi_configuration.manager.ConnectorUtils.unregisterReceiver;
import static com.thanosfisherman.elvis.Elvis.of;

public final class WifiUtils implements WifiConnectorBuilder,
        WifiConnectorBuilder.WifiUtilsBuilder,
        WifiConnectorBuilder.WifiSuccessListener,
        WifiConnectorBuilder.WifiWpsSuccessListener {
    
    private final WifiManager mWifiManager;
    
    private final Context mContext;
    private static boolean mEnableLog;
    private long mWpsTimeoutMillis = 30000;
    private long mTimeoutMillis = 30000;
    
    private static final String TAG = WifiUtils.class.getSimpleName();
    // private static final WifiUtils INSTANCE = new WifiUtils();
    
    private final WifiStateReceiver mWifiStateReceiver;
    
    private final WifiConnectionReceiver mWifiConnectionReceiver;
    
    private final WifiScanReceiver mWifiScanReceiver;
    
    private String mSsid;
    
    private String mBssid;
    
    private String mPassword;
    
    private ScanResult mSingleScanResult;
    
    private ScanResultsListener mScanResultsListener;
    
    private ConnectionScanResultsListener mConnectionScanResultsListener;
    
    private ConnectionSuccessListener mConnectionSuccessListener;
    
    private WifiStateListener mWifiStateListener;
    
    private ConnectionWpsListener mConnectionWpsListener;

    
    private final WifiStateCallback mWifiStateCallback = new WifiStateCallback() {
        @Override
        public void onWifiEnabled() {
            wifiLog("WIFI ENABLED...");
            unregisterReceiver(mContext, mWifiStateReceiver);
            of(mWifiStateListener).ifPresent(stateListener -> stateListener.isSuccess(true));

            if (mScanResultsListener != null || mPassword != null) {
                wifiLog("START SCANNING....");
                if (mWifiManager.startScan())
                    registerReceiver(mContext, mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                else {
                    of(mScanResultsListener).ifPresent(resultsListener -> resultsListener.onScanResults(new ArrayList<>()));
                    of(mConnectionWpsListener).ifPresent(wpsListener -> wpsListener.isSuccessful(false));
                    mWifiConnectionCallback.errorConnect();
                    wifiLog("ERROR COULDN'T SCAN");
                }
            }
        }
    };

    
    private final WifiScanCallback mWifiScanResultsCallback = new WifiScanCallback() {
        @Override
        public void onScanResultsReady() {
            wifiLog("GOT SCAN RESULTS");
            unregisterReceiver(mContext, mWifiScanReceiver);

            final List<ScanResult> scanResultList = mWifiManager.getScanResults();
            of(mScanResultsListener).ifPresent(resultsListener -> resultsListener.onScanResults(scanResultList));
            of(mConnectionScanResultsListener).ifPresent(connectionResultsListener -> mSingleScanResult = connectionResultsListener.onConnectWithScanResult(scanResultList));

            if (mConnectionWpsListener != null && mBssid != null && mPassword != null) {
                mSingleScanResult = matchScanResultBssid(mBssid, scanResultList);
                if (mSingleScanResult != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    connectWps(mWifiManager, mSingleScanResult, mPassword, mWpsTimeoutMillis, mConnectionWpsListener);
                else {
                    if (mSingleScanResult == null)
                        wifiLog("Couldn't find network. Possibly out of range");
                    mConnectionWpsListener.isSuccessful(false);
                }
                return;
            }

            if (mSsid != null) {
                if (mBssid != null)
                    mSingleScanResult = matchScanResult(mSsid, mBssid, scanResultList);
                else
                    mSingleScanResult = matchScanResultSsid(mSsid, scanResultList);
            }
            if (mSingleScanResult != null && mPassword != null) {
                if (connectToWifi(mContext, mWifiManager, mSingleScanResult, mPassword)) {
                    registerReceiver(mContext, mWifiConnectionReceiver.activateTimeoutHandler(mSingleScanResult),
                            new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
                    registerReceiver(mContext, mWifiConnectionReceiver,
                            new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                } else
                    mWifiConnectionCallback.errorConnect();
            } else
                mWifiConnectionCallback.errorConnect();
        }
    };

    public List<ScanResult> getScanWifiResult(){

        if (mWifiManager != null && mWifiManager.getScanResults() != null && mWifiManager.getScanResults().size() > 0) {
            return mWifiManager.getScanResults();
        }
        return null;
    }

    
    private final WifiConnectionCallback mWifiConnectionCallback = new WifiConnectionCallback() {
        @Override
        public void successfulConnect() {
            wifiLog("CONNECTED SUCCESSFULLY");
            unregisterReceiver(mContext, mWifiConnectionReceiver);
            //reenableAllHotspots(mWifiManager);
            of(mConnectionSuccessListener).ifPresent(successListener -> successListener.isSuccessful(true));
        }

        @Override
        public void errorConnect() {
            unregisterReceiver(mContext, mWifiConnectionReceiver);
            reenableAllHotspots(mWifiManager);
            //if (mSingleScanResult != null)
            //cleanPreviousConfiguration(mWifiManager, mSingleScanResult);
            of(mConnectionSuccessListener).ifPresent(successListener -> {
                successListener.isSuccessful(false);
                wifiLog("DIDN'T CONNECT TO WIFI");
            });
        }
    };

    public WifiUtils( Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager == null)
            throw new RuntimeException("WifiManager is not supposed to be null");
        mWifiStateReceiver = new WifiStateReceiver(mWifiStateCallback);
        mWifiScanReceiver = new WifiScanReceiver(mWifiScanResultsCallback);
        mWifiConnectionReceiver = new WifiConnectionReceiver(mWifiConnectionCallback, mWifiManager, mTimeoutMillis);
    }

    public static WifiUtilsBuilder withContext( final Context context) {
        return new WifiUtils(context);
    }

    public static void wifiLog(final String text) {
        if (mEnableLog)
            Log.d(TAG, "WifiUtils: " + text);
    }

    public static void enableLog(final boolean enabled) {
        mEnableLog = enabled;
    }

    @Override
    public void enableWifi( final WifiStateListener wifiStateListener) {
        mWifiStateListener = wifiStateListener;
        if (mWifiManager.isWifiEnabled())
            mWifiStateCallback.onWifiEnabled();
        else {
            if (mWifiManager.setWifiEnabled(true))
                registerReceiver(mContext, mWifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            else {
                of(wifiStateListener).ifPresent(stateListener -> stateListener.isSuccess(false));
                of(mScanResultsListener).ifPresent(resultsListener -> resultsListener.onScanResults(new ArrayList<>()));
                of(mConnectionWpsListener).ifPresent(wpsListener -> wpsListener.isSuccessful(false));
                mWifiConnectionCallback.errorConnect();
                wifiLog("COULDN'T ENABLE WIFI");
            }
        }
    }

    @Override
    public void enableWifi() {
        enableWifi(null);
    }

    
    @Override
    public WifiConnectorBuilder scanWifi(final ScanResultsListener scanResultsListener) {
        mScanResultsListener = scanResultsListener;
        return this;
    }

    
    @Override
    public WifiSuccessListener connectWith( final String ssid,  final String password) {
        mSsid = ssid;
        mPassword = password;
        return this;
    }

    
    @Override
    public WifiSuccessListener connectWith( final String ssid,  final String bssid,  final String password) {
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        return this;
    }

    
    @Override
    public WifiSuccessListener connectWithScanResult( final String password,
                                                      final ConnectionScanResultsListener connectionScanResultsListener) {
        mConnectionScanResultsListener = connectionScanResultsListener;
        mPassword = password;
        return this;
    }

    
    @Override
    public WifiWpsSuccessListener connectWithWps( final String bssid,  final String password) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // only for gingerbread and newer versions
            mBssid = bssid;
            mPassword = password;
        }

        return this;
    }

    @Override
    public void cancelAutoConnect() {
        unregisterReceiver(mContext, mWifiStateReceiver);
        unregisterReceiver(mContext, mWifiScanReceiver);
        unregisterReceiver(mContext, mWifiConnectionReceiver);
        of(mSingleScanResult).ifPresent(scanResult -> cleanPreviousConfiguration(mWifiManager, scanResult));
        reenableAllHotspots(mWifiManager);
    }

    
    @Override
    public WifiSuccessListener setTimeout(final long timeOutMillis) {
        mTimeoutMillis = timeOutMillis;
        mWifiConnectionReceiver.setTimeout(timeOutMillis);
        return this;
    }

    
    @Override
    public WifiWpsSuccessListener setWpsTimeout(final long timeOutMillis) {
        mWpsTimeoutMillis = timeOutMillis;
        return this;
    }

    
    @Override
    public WifiConnectorBuilder onConnectionWpsResult( final ConnectionWpsListener successListener) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // only for gingerbread and newer versions
            mConnectionWpsListener = successListener;
        }
        return this;
    }


    
    @Override
    public WifiConnectorBuilder onConnectionResult( final ConnectionSuccessListener successListener) {
        mConnectionSuccessListener = successListener;
        return this;
    }

    @Override
    public void start() {
        unregisterReceiver(mContext, mWifiStateReceiver);
        unregisterReceiver(mContext, mWifiScanReceiver);
        unregisterReceiver(mContext, mWifiConnectionReceiver);
        enableWifi(null);
    }

    @Override
    public void disableWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
            unregisterReceiver(mContext, mWifiStateReceiver);
            unregisterReceiver(mContext, mWifiScanReceiver);
            unregisterReceiver(mContext, mWifiConnectionReceiver);
        }
        wifiLog("WiFi Disabled");
    }
}
