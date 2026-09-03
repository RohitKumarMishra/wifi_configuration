import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'wifi_configuration_platform_interface.dart';

/// Method-channel implementation of [WifiConfigurationPlatform].
class MethodChannelWifiConfiguration extends WifiConfigurationPlatform {
  /// The method channel used to talk to Android and iOS.
  @visibleForTesting
  final MethodChannel methodChannel = const MethodChannel('wifi_configuration');

  @override
  Future<String?> getPlatformVersion() async {
    return methodChannel.invokeMethod<String>('getPlatformVersion');
  }

  @override
  Future<String> connectToWifi({
    required String ssid,
    required String password,
    String? packageName,
  }) async {
    final String? status = await methodChannel.invokeMethod<String>(
      'connectToWifi',
      <String, dynamic>{
        'ssid': ssid,
        'password': password,
        'packageName': packageName,
      },
    );
    return status ?? 'notConnected';
  }

  @override
  Future<List<String>> getWifiList() async {
    final List<dynamic>? raw = await methodChannel.invokeMethod<List<dynamic>>(
      'getWifiList',
    );
    if (raw == null) {
      return const <String>[];
    }
    return raw.map((dynamic item) => '$item').toList();
  }

  @override
  Future<bool> isConnectedToWifi(String ssid) async {
    final bool? connected = await methodChannel.invokeMethod<bool>(
      'isConnectedToWifi',
      <String, dynamic>{'ssid': ssid},
    );
    return connected ?? false;
  }

  @override
  Future<String> connectedToWifi() async {
    final String? ssid = await methodChannel.invokeMethod<String>(
      'connectedToWifi',
    );
    return ssid ?? '';
  }
}
