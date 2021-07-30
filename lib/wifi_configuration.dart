import 'dart:async';

import 'package:flutter/services.dart';

enum WifiConnectionStatus {
  connected,
  alreadyConnected,
  notConnected,
  platformNotSupported,
  profileAlreadyInstalled,
  locationNotAllowed,
}

class WifiConfiguration {
  static const MethodChannel _channel =
      const MethodChannel('wifi_configuration');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');

    return version;
  }

  static Future<WifiConnectionStatus> connectToWifi(
      String ssid, String password, String packageName) async {
    final String isConnected = await _channel.invokeMethod(
        'connectToWifi', <String, dynamic>{
      "ssid": ssid,
      "password": password,
      "packageName": packageName
    });
    WifiConnectionStatus? status;
    switch (isConnected) {
      case "connected":
        status = WifiConnectionStatus.connected;
        break;
      case "alreadyConnected":
        status = WifiConnectionStatus.alreadyConnected;
        break;
      case "notConnected":
        status = WifiConnectionStatus.notConnected;
        break;
      case "platformNotSupported":
        status = WifiConnectionStatus.platformNotSupported;
        break;
      case "profileAlreadyInstalled":
        status = WifiConnectionStatus.profileAlreadyInstalled;
        break;
      case "locationNotAllowed":
        status = WifiConnectionStatus.locationNotAllowed;
        break;
    }
    return status!;
  }

  static Future<List<dynamic>> getWifiList() async {
    final List<dynamic> wifiList = await _channel.invokeMethod('getWifiList');
    return wifiList;
  }

  static Future<bool> isConnectedToWifi(String ssid) async {
    final bool isConnected = await _channel
        .invokeMethod('isConnectedToWifi', <String, dynamic>{"ssid": ssid});
    return isConnected;
  }

  static Future<String> connectedToWifi() async {
    final String connectedWifiName =
        await _channel.invokeMethod('connectedToWifi');
    return connectedWifiName;
  }
}
