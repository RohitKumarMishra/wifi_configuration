package com.example.wifi_configuration_example;

import android.os.Bundle;
import android.util.Log;

import com.example.wifi_configuration.manager.WifiUtils;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {

  private WifiUtils wifiUtils;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
    wifiUtils = new WifiUtils(this);
//    connectWithWPA("Rk", "123456@123");


  }

}
