import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:wifi_configuration/wifi_configuration.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}




class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    getConnectionState();
  }

void getConnectionState() async {
  var listAvailableWifi = await WifiConfiguration.getWifiList();
  print("get wifi list : " + listAvailableWifi.toString());
    bool connectionState = await WifiConfiguration.connectToWifi("AndroidAPAF", "feedh@12345");
    print("is Connected : ${connectionState}");

}




  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
      ),
    );
  }
}
