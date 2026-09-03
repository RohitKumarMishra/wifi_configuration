package com.rohitkrmishra.wifi_configuration

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** WifiConfigurationPlugin */
class WifiConfigurationPlugin :
    FlutterPlugin,
    MethodCallHandler,
    ActivityAware,
    PluginRegistry.RequestPermissionsResultListener {
    private lateinit var channel: MethodChannel
    private lateinit var appContext: Context
    private var activity: Activity? = null
    private var activityBinding: ActivityPluginBinding? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingPermissionResult: Result? = null
    private var pendingPermissionCall: MethodCall? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var connectTimeout: Runnable? = null
    private var connectResult: Result? = null
    private var scanResult: Result? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        appContext = binding.applicationContext
        channel = MethodChannel(binding.binaryMessenger, "wifi_configuration")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" ->
                result.success("Android ${Build.VERSION.RELEASE}")
            "connectToWifi" ->
                withLocationAccess(call, result) { connectToWifi(call, result) }
            "getWifiList" ->
                withLocationAccess(call, result) { scanWifi(result) }
            "isConnectedToWifi" -> {
                val ssid = call.argument<String>("ssid").orEmpty()
                result.success(normalizeSsid(currentSsid()) == normalizeSsid(ssid))
            }
            "connectedToWifi" ->
                withLocationAccess(call, result) {
                    result.success(currentSsid())
                }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        cancelPendingConnect()
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activityBinding?.removeRequestPermissionsResultListener(this)
        activity = null
        activityBinding = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        onDetachedFromActivityForConfigChanges()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ): Boolean {
        if (requestCode != PERMISSION_REQUEST) {
            return false
        }
        val call = pendingPermissionCall
        val pending = pendingPermissionResult
        pendingPermissionCall = null
        pendingPermissionResult = null
        if (pending == null || call == null) {
            return true
        }
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onMethodCall(call, pending)
        } else {
            when (call.method) {
                "connectToWifi" -> pending.success("locationNotAllowed")
                "connectedToWifi" -> pending.success("")
                "getWifiList" -> pending.success(emptyList<String>())
                else -> pending.success(null)
            }
        }
        return true
    }

    private fun withLocationAccess(call: MethodCall, result: Result, onGranted: () -> Unit) {
        if (hasWifiPermissions() && isLocationEnabled()) {
            onGranted()
            return
        }
        if (!isLocationEnabled()) {
            when (call.method) {
                "connectToWifi" -> result.success("locationNotAllowed")
                "connectedToWifi" -> result.success("")
                "getWifiList" -> result.success(emptyList<String>())
                else -> result.success(null)
            }
            return
        }
        val host = activity
        if (host == null) {
            when (call.method) {
                "connectToWifi" -> result.success("locationNotAllowed")
                else -> result.success(if (call.method == "getWifiList") emptyList<String>() else "")
            }
            return
        }
        pendingPermissionCall = call
        pendingPermissionResult = result
        ActivityCompat.requestPermissions(host, requiredPermissions(), PERMISSION_REQUEST)
    }

    private fun connectToWifi(call: MethodCall, result: Result) {
        val ssid = call.argument<String>("ssid").orEmpty()
        val password = call.argument<String>("password").orEmpty()
        if (ssid.isEmpty()) {
            result.success("notConnected")
            return
        }
        if (normalizeSsid(currentSsid()) == normalizeSsid(ssid)) {
            result.success("alreadyConnected")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectWithSpecifier(ssid, password, result)
        } else {
            connectLegacy(ssid, password, result)
        }
    }

    private fun connectWithSpecifier(ssid: String, password: String, result: Result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            result.success("platformNotSupported")
            return
        }
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(ssid)
        if (password.isNotEmpty()) {
            specifierBuilder.setWpa2Passphrase(password)
        }
        val request =
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifierBuilder.build())
                .build()

        cancelPendingConnect()
        connectResult = result
        val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.bindProcessToNetwork(network)
                    // Keep this callback registered: specifier networks are
                    // torn down when the callback is unregistered.
                    finishConnect("connected", unregisterCallback = false)
                }

                override fun onUnavailable() {
                    finishConnect("notConnected", unregisterCallback = true)
                }

                override fun onLost(network: Network) {
                    connectivityManager.bindProcessToNetwork(null)
                }
            }
        networkCallback = callback
        val timeout =
            Runnable {
                finishConnect("notConnected", unregisterCallback = true)
            }
        connectTimeout = timeout
        mainHandler.postDelayed(timeout, CONNECT_TIMEOUT_MS)
        connectivityManager.requestNetwork(request, callback)
    }

    private fun connectLegacy(ssid: String, password: String, result: Result) {
        val wifiManager =
            appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        val config = android.net.wifi.WifiConfiguration()
        config.SSID = "\"$ssid\""
        if (password.isEmpty()) {
            @Suppress("DEPRECATION")
            config.allowedKeyManagement.set(android.net.wifi.WifiConfiguration.KeyMgmt.NONE)
        } else {
            config.preSharedKey = "\"$password\""
        }
        @Suppress("DEPRECATION")
        val networkId = wifiManager.addNetwork(config)
        if (networkId == -1) {
            result.success("notConnected")
            return
        }
        @Suppress("DEPRECATION")
        val enabled = wifiManager.enableNetwork(networkId, true)
        result.success(if (enabled) "connected" else "notConnected")
    }

    private fun scanWifi(result: Result) {
        val wifiManager =
            appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val cached = currentScanSsids(wifiManager)
        @Suppress("DEPRECATION")
        val started = wifiManager.startScan()
        if (!started) {
            result.success(cached)
            return
        }
        scanResult = result
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        lateinit var receiver: BroadcastReceiver
        val timeout =
            Runnable {
                try {
                    appContext.unregisterReceiver(receiver)
                } catch (_: IllegalArgumentException) {
                    // Already unregistered.
                }
                finishScan(currentScanSsids(wifiManager).ifEmpty { cached })
            }
        receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    mainHandler.removeCallbacks(timeout)
                    try {
                        appContext.unregisterReceiver(this)
                    } catch (_: IllegalArgumentException) {
                        // Already unregistered.
                    }
                    finishScan(currentScanSsids(wifiManager).ifEmpty { cached })
                }
            }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )
        mainHandler.postDelayed(timeout, SCAN_TIMEOUT_MS)
    }

    private fun currentScanSsids(wifiManager: WifiManager): List<String> {
        if (!hasWifiPermissions()) {
            return emptyList()
        }
        @Suppress("DEPRECATION")
        return wifiManager.scanResults
            .map { normalizeSsid(it.SSID) }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun currentSsid(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectivityManager =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return ""
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ""
            val transportInfo = capabilities.transportInfo
            if (transportInfo is android.net.wifi.WifiInfo) {
                return normalizeSsid(transportInfo.ssid)
            }
        }
        val wifiManager =
            appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        return normalizeSsid(wifiManager.connectionInfo?.ssid)
    }

    private fun finishConnect(status: String, unregisterCallback: Boolean) {
        connectTimeout?.let { mainHandler.removeCallbacks(it) }
        connectTimeout = null
        val pending = connectResult ?: return
        connectResult = null
        if (unregisterCallback) {
            val callback = networkCallback
            networkCallback = null
            if (callback != null) {
                val connectivityManager =
                    appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (_: IllegalArgumentException) {
                    // Already unregistered.
                }
                connectivityManager.bindProcessToNetwork(null)
            }
        }
        pending.success(status)
    }

    private fun finishScan(ssids: List<String>) {
        val pending = scanResult ?: return
        scanResult = null
        pending.success(ssids)
    }

    private fun cancelPendingConnect() {
        connectTimeout?.let { mainHandler.removeCallbacks(it) }
        connectTimeout = null
        connectResult?.success("notConnected")
        connectResult = null
        val callback = networkCallback ?: return
        networkCallback = null
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: IllegalArgumentException) {
            // Already unregistered.
        }
        connectivityManager.bindProcessToNetwork(null)
    }

    private fun hasWifiPermissions(): Boolean {
        return requiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(appContext, permission) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredPermissions(): Array<String> {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        return permissions.toTypedArray()
    }

    private fun isLocationEnabled(): Boolean {
        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun normalizeSsid(raw: String?): String {
        if (raw.isNullOrBlank() || raw.contains("unknown ssid", ignoreCase = true)) {
            return ""
        }
        return raw.trim().removePrefix("\"").removeSuffix("\"")
    }

    companion object {
        private const val PERMISSION_REQUEST = 0x5749
        private const val CONNECT_TIMEOUT_MS = 30_000L
        private const val SCAN_TIMEOUT_MS = 5_000L
    }
}
