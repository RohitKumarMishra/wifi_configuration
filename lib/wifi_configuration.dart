import 'dart:async';

import 'package:flutter/services.dart';

class WifiConfiguration {
  static const MethodChannel _channel =
      const MethodChannel('wifi_configuration');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');

    return version;
  }

  static Future<String> connectToWifi(
    String ssid,
    String password,
      String packageName
  ) async {
    final String isConnected = await _channel.invokeMethod(
        'connectToWifi', <String, dynamic>{"ssid": ssid, "password": password, "packageName" : packageName});
    return isConnected;
  }

  static Future<List<dynamic>> getWifiList() async {
    final List<dynamic> wifiList = await _channel.invokeMethod('getWifiList');
    return wifiList;
  }

  static Future<bool> isConnectedToWifi(String ssid) async {
    final bool isConnected = await _channel.invokeMethod('isConnectedToWifi',<String, dynamic>{"ssid": ssid});
    return isConnected;
  }


  static Future<String> connectedToWifi() async {
    final String connectedWifiName = await _channel.invokeMethod('connectedToWifi');
    return connectedWifiName;
  }

}
