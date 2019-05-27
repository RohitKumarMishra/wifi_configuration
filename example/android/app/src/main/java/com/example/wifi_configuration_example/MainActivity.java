package com.example.wifi_configuration_example;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.wifi_configuration.manager.WifiUtils;

import io.flutter.app.FlutterActivity;
import io.flutter.plugins.GeneratedPluginRegistrant;

import static com.example.wifi_configuration.PermissionHelper.FINE_LOCATION_PERMISSION;

public class MainActivity extends FlutterActivity {
  private GeneratedPluginRegistrant  generatedPluginRegistrant = new GeneratedPluginRegistrant();

  private WifiUtils wifiUtils;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);


    wifiUtils = new WifiUtils(this);
//    connectWithWPA("Rk", "123456@123");


  }

}
