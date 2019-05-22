package com.example.wifi_configuration;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.example.wifi_configuration.connect.ConnectionSuccessListener;
import com.example.wifi_configuration.connect.WifiCallback;
import com.example.wifi_configuration.manager.WifiUtils;

/** WifiConfigurationPlugin */
public class WifiConfigurationPlugin implements MethodCallHandler {
  static Context context;
  private static WifiUtils wifiUtils;


  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "wifi_configuration");
    context = registrar.context();
    channel.setMethodCallHandler(new WifiConfigurationPlugin());
    wifiUtils = new WifiUtils(context);

  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
   if (call.method.equals("connectToWifi")) {
      this.connectWithWPA(call.argument("ssid"), call.argument("password"), WifiConfigurationPlugin.context, result);

    } else if (call.method.equals("getWifiList")) {
      result.success(this.getAvailableWifiList());

    }
    else {
      result.notImplemented();
    }
  }




  private void connectWithWPA(String ssid, String password, Context context, Result result) {

    wifiUtils.withContext(context)
            .connectWith(ssid +
                    "", password)
            .setTimeout(40000)
            .onConnectionResult(new ConnectionSuccessListener() {
              @Override
              public void isSuccessful(boolean isSuccess) {
                result.success(isSuccess);

              }
            })
            .start();

  }



  private  List<String> getAvailableWifiList() {
    List<String> wifiList = new ArrayList<String>();
    if (wifiUtils.getScanWifiResult() != null){
      Log.d("WifiResults-->", wifiUtils.getScanWifiResult()+"");

      for (ScanResult wifiName : wifiUtils.getScanWifiResult()
           ) {
        Log.d("WifiUtils", wifiName.SSID);
        wifiList.add(wifiName.SSID);
      }
      return wifiList;
    }
    return null;
  }

}
