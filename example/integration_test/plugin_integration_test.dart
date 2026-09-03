import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:wifi_configuration/wifi_configuration.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('platform version is reported', (WidgetTester tester) async {
    final String? version = await WifiConfiguration.platformVersion;
    expect(version?.isNotEmpty, isTrue);
  });
}
