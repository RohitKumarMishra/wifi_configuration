import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'wifi_configuration_method_channel.dart';

/// The interface that implementations of wifi_configuration must implement.
abstract class WifiConfigurationPlatform extends PlatformInterface {
  /// Constructs a [WifiConfigurationPlatform].
  WifiConfigurationPlatform() : super(token: _token);

  static final Object _token = Object();

  static WifiConfigurationPlatform _instance = MethodChannelWifiConfiguration();

  /// The default instance of [WifiConfigurationPlatform] to use.
  ///
  /// Defaults to [MethodChannelWifiConfiguration].
  static WifiConfigurationPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [WifiConfigurationPlatform] when
  /// they register themselves.
  static set instance(WifiConfigurationPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// OS version string from the host platform.
  Future<String?> getPlatformVersion() {
    throw UnimplementedError('getPlatformVersion() has not been implemented.');
  }

  /// Connects to [ssid]. Returns a [WifiConnectionStatus] name.
  Future<String> connectToWifi({
    required String ssid,
    required String password,
    String? packageName,
  }) {
    throw UnimplementedError('connectToWifi() has not been implemented.');
  }

  /// Nearby or configured Wi-Fi SSIDs.
  Future<List<String>> getWifiList() {
    throw UnimplementedError('getWifiList() has not been implemented.');
  }

  /// Whether the current SSID matches [ssid].
  Future<bool> isConnectedToWifi(String ssid) {
    throw UnimplementedError('isConnectedToWifi() has not been implemented.');
  }

  /// Current SSID, or empty when unknown.
  Future<String> connectedToWifi() {
    throw UnimplementedError('connectedToWifi() has not been implemented.');
  }
}
