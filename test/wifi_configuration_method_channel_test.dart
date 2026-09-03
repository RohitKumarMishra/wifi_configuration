import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wifi_configuration/wifi_configuration_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  final MethodChannelWifiConfiguration platform =
      MethodChannelWifiConfiguration();
  const MethodChannel channel = MethodChannel('wifi_configuration');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
          switch (methodCall.method) {
            case 'getPlatformVersion':
              return '42';
            case 'connectToWifi':
              return 'connected';
            case 'getWifiList':
              return <String>['OfficeNet'];
            case 'isConnectedToWifi':
              return true;
            case 'connectedToWifi':
              return 'OfficeNet';
            default:
              return null;
          }
        });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });

  test('connect and list', () async {
    expect(
      await platform.connectToWifi(ssid: 'OfficeNet', password: 'secret'),
      'connected',
    );
    expect(await platform.getWifiList(), <String>['OfficeNet']);
    expect(await platform.isConnectedToWifi('OfficeNet'), isTrue);
    expect(await platform.connectedToWifi(), 'OfficeNet');
  });
}
