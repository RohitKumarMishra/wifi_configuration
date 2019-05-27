import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPlugin {
  static const MethodChannel _channel =
      const MethodChannel('flutter_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> connectToWifi(String ssid, String password) async {

    final bool isConnected = await _channel.invokeMethod('connectToWifi', <String, dynamic>{"ssid" : ssid, "password" : password});
    return isConnected;
  }


  static Future<List<String>> getWifiList() async {

    final List<String> wifiList = await _channel.invokeMethod('getWifiList');
    return wifiList;
  }

//  static Future<bool> get connectWithWifi async {
//    return true;
//  }

  static Future<bool> isConnectedToWifi(String ssid) async {
    final bool isConnected = await _channel.invokeMethod('isConnectedToWifi');
    return isConnected;
  }


}
