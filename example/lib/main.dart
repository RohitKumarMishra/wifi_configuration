import 'package:flutter/material.dart';
import 'package:wifi_configuration/wifi_configuration.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final TextEditingController _ssid = TextEditingController();
  final TextEditingController _password = TextEditingController();
  String _status = 'Idle';
  String _currentSsid = '';
  List<String> _networks = const <String>[];

  @override
  void initState() {
    super.initState();
    _refreshCurrent();
  }

  @override
  void dispose() {
    _ssid.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _refreshCurrent() async {
    final String ssid = await WifiConfiguration.connectedToWifi();
    if (!mounted) {
      return;
    }
    setState(() {
      _currentSsid = ssid.isEmpty ? '(unknown)' : ssid;
    });
  }

  Future<void> _scan() async {
    setState(() => _status = 'Scanning…');
    final List<String> networks = await WifiConfiguration.getWifiList();
    if (!mounted) {
      return;
    }
    setState(() {
      _networks = networks;
      _status = networks.isEmpty
          ? 'No networks returned'
          : 'Found ${networks.length}';
    });
  }

  Future<void> _connect() async {
    final String ssid = _ssid.text.trim();
    if (ssid.isEmpty) {
      setState(() => _status = 'Enter an SSID');
      return;
    }
    setState(() => _status = 'Connecting…');
    final WifiConnectionStatus result = await WifiConfiguration.connectToWifi(
      ssid,
      _password.text,
    );
    if (!mounted) {
      return;
    }
    setState(() => _status = result.name);
    await _refreshCurrent();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('wifi_configuration example')),
        body: ListView(
          padding: const EdgeInsets.all(16),
          children: <Widget>[
            Text('Current SSID: $_currentSsid'),
            const SizedBox(height: 8),
            Text('Status: $_status'),
            const SizedBox(height: 16),
            TextField(
              controller: _ssid,
              decoration: const InputDecoration(
                labelText: 'SSID',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _password,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Password (empty for open networks)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              children: <Widget>[
                FilledButton(onPressed: _connect, child: const Text('Connect')),
                OutlinedButton(onPressed: _scan, child: const Text('Scan')),
                OutlinedButton(
                  onPressed: _refreshCurrent,
                  child: const Text('Refresh SSID'),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text('Networks'),
            const SizedBox(height: 8),
            if (_networks.isEmpty) const Text('Run Scan to load SSIDs.'),
            ..._networks.map(
              (String ssid) => ListTile(
                contentPadding: EdgeInsets.zero,
                title: Text(ssid),
                onTap: () => _ssid.text = ssid,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
