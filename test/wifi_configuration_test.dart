import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:wifi_configuration/wifi_configuration.dart';
import 'package:wifi_configuration/wifi_configuration_method_channel.dart';
import 'package:wifi_configuration/wifi_configuration_platform_interface.dart';

class MockWifiConfigurationPlatform
    with MockPlatformInterfaceMixin
    implements WifiConfigurationPlatform {
  @override
  Future<String?> getPlatformVersion() => Future<String?>.value('42');

  @override
  Future<String> connectToWifi({
    required String ssid,
    required String password,
    String? packageName,
  }) {
    if (ssid.isEmpty) {
      return Future<String>.value('notConnected');
    }
    return Future<String>.value('connected');
  }

  @override
  Future<List<String>> getWifiList() {
    return Future<List<String>>.value(const <String>['OfficeNet', 'Guest']);
  }

  @override
  Future<bool> isConnectedToWifi(String ssid) {
    return Future<bool>.value(ssid == 'OfficeNet');
  }

  @override
  Future<String> connectedToWifi() {
    return Future<String>.value('OfficeNet');
  }
}

void main() {
  final WifiConfigurationPlatform initialPlatform =
      WifiConfigurationPlatform.instance;

  test('$MethodChannelWifiConfiguration is the default instance', () {
    expect(initialPlatform, isA<MethodChannelWifiConfiguration>());
  });

  test('platform version', () async {
    final MockWifiConfigurationPlatform fakePlatform =
        MockWifiConfigurationPlatform();
    WifiConfigurationPlatform.instance = fakePlatform;
    expect(await WifiConfiguration.platformVersion, '42');
  });

  test('connectToWifi maps status strings', () async {
    final MockWifiConfigurationPlatform fakePlatform =
        MockWifiConfigurationPlatform();
    WifiConfigurationPlatform.instance = fakePlatform;
    expect(
      await WifiConfiguration.connectToWifi('OfficeNet', 'secret'),
      WifiConnectionStatus.connected,
    );
    expect(
      await WifiConfiguration.connectToWifi('', ''),
      WifiConnectionStatus.notConnected,
    );
  });

  test('wifi list and current ssid', () async {
    final MockWifiConfigurationPlatform fakePlatform =
        MockWifiConfigurationPlatform();
    WifiConfigurationPlatform.instance = fakePlatform;
    expect(await WifiConfiguration.getWifiList(), <String>[
      'OfficeNet',
      'Guest',
    ]);
    expect(await WifiConfiguration.connectedToWifi(), 'OfficeNet');
    expect(await WifiConfiguration.isConnectedToWifi('OfficeNet'), isTrue);
    expect(await WifiConfiguration.isConnectedToWifi('Other'), isFalse);
  });
}
