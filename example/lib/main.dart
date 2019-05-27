import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:wifi_configuration/wifi_configuration.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}


//enum wifiStatus {
//  conected,
//alreadyConnected,
//notConnected ,
//platformNotSupported,
//profileAlreadyInstalled,
//
//}

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
  String connectionState = await WifiConfiguration.connectToWifi("DBWSN5", "DarkBe@rs");
    print("is Connected : ${connectionState}");


    switch (connectionState) {
      case "connected":
        print("connected");
        break;

      case "alreadyConnected":
        print("alreadyConnected");
        break;

      case "notConnected":
        print("notConnected");
        break;

      case "platformNotSupported":
        print("platformNotSupported");
        break;

      case "profileAlreadyInstalled":
        print("profileAlreadyInstalled");
        break;

    case "locationNotAllowed":
      print("locationNotAllowed");
      break;
    }

    bool isConnected = await WifiConfiguration.isConnectedToWifi("DBWSN5");
    print("coneection status ${isConnected}");

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
