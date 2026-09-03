import 'package:flutter_test/flutter_test.dart';
import 'package:wifi_configuration_example/main.dart';

void main() {
  testWidgets('example shows wifi_configuration title', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const MyApp());
    expect(find.text('wifi_configuration example'), findsOneWidget);
    expect(find.text('Connect'), findsOneWidget);
    expect(find.text('Scan'), findsOneWidget);
  });
}
