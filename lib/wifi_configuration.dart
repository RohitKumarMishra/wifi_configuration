import 'wifi_configuration_platform_interface.dart';

/// Result of a [WifiConfiguration.connectToWifi] attempt.
enum WifiConnectionStatus {
  /// Joined the requested network.
  connected,

  /// Already associated with that SSID.
  alreadyConnected,

  /// Could not join the network.
  notConnected,

  /// This platform or OS version cannot configure Wi-Fi.
  platformNotSupported,

  /// An iOS configuration profile for this SSID is already installed.
  profileAlreadyInstalled,

  /// Location permission or location services are required and were denied.
  locationNotAllowed,
}

/// Connect to Wi-Fi, inspect the current SSID, and list nearby networks.
///
/// Android uses [WifiNetworkSpecifier] on API 29+ and the older
/// `WifiConfiguration` APIs below that. iOS uses `NEHotspotConfiguration`.
class WifiConfiguration {
  WifiConfiguration._();

  /// OS version string, mainly for the example app and tests.
  static Future<String?> get platformVersion {
    return WifiConfigurationPlatform.instance.getPlatformVersion();
  }

  /// Connects the device to [ssid] using [password].
  ///
  /// [packageName] is kept for compatibility with 1.x. On modern Android it is
  /// unused; location is requested at runtime instead of sending the user to
  /// app settings.
  ///
  /// Returns [WifiConnectionStatus.notConnected] when the platform reports an
  /// unrecognized status string.
  static Future<WifiConnectionStatus> connectToWifi(
    String ssid,
    String password, [
    String? packageName,
  ]) async {
    final String status = await WifiConfigurationPlatform.instance
        .connectToWifi(
          ssid: ssid,
          password: password,
          packageName: packageName,
        );
    return switch (status) {
      'connected' => WifiConnectionStatus.connected,
      'alreadyConnected' => WifiConnectionStatus.alreadyConnected,
      'notConnected' => WifiConnectionStatus.notConnected,
      'platformNotSupported' => WifiConnectionStatus.platformNotSupported,
      'profileAlreadyInstalled' => WifiConnectionStatus.profileAlreadyInstalled,
      'locationNotAllowed' => WifiConnectionStatus.locationNotAllowed,
      _ => WifiConnectionStatus.notConnected,
    };
  }

  /// Nearby SSIDs on Android (requires location). On iOS, configured hotspot
  /// SSIDs only — Apple does not allow scanning nearby networks.
  static Future<List<String>> getWifiList() async {
    final List<String> networks = await WifiConfigurationPlatform.instance
        .getWifiList();
    return networks.where((String ssid) => ssid.isNotEmpty).toList();
  }

  /// Whether the device is currently associated with [ssid].
  static Future<bool> isConnectedToWifi(String ssid) {
    return WifiConfigurationPlatform.instance.isConnectedToWifi(ssid);
  }

  /// Current Wi-Fi SSID, or an empty string when it cannot be read.
  ///
  /// Android and iOS both require location permission (and on iOS, the Access
  /// Wi-Fi Information entitlement) to return a real name.
  static Future<String> connectedToWifi() {
    return WifiConfigurationPlatform.instance.connectedToWifi();
  }
}
