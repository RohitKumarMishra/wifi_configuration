import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wifi_configuration/wifi_configuration.dart';

void main() {
  const MethodChannel channel = MethodChannel('wifi_configuration');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await WifiConfiguration.platformVersion, '42');
  });
}
