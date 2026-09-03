# wifi_configuration

Connect a Flutter app to a Wi-Fi network, read the current SSID, and list
available networks on Android and iOS.

This is a Dart 3 rewrite of `wifi_configuration` 1.2.1. The Dart API is the
same (`connectToWifi`, `getWifiList`, `isConnectedToWifi`, `connectedToWifi`).

## Install

```yaml
dependencies:
  wifi_configuration: ^2.0.0
```

## Usage

```dart
import 'package:wifi_configuration/wifi_configuration.dart';

final WifiConnectionStatus status = await WifiConfiguration.connectToWifi(
  'OfficeNet',
  'passphrase',
);

switch (status) {
  case WifiConnectionStatus.connected:
  case WifiConnectionStatus.alreadyConnected:
    break;
  case WifiConnectionStatus.locationNotAllowed:
    // Ask the user to enable location / grant permission.
    break;
  default:
    break;
}

final List<String> nearby = await WifiConfiguration.getWifiList();
final String current = await WifiConfiguration.connectedToWifi();
final bool matches = await WifiConfiguration.isConnectedToWifi('OfficeNet');
```

`packageName` from 1.x is still accepted as an optional third argument and is
ignored on current Android/iOS.

## Platform notes

### Android

Add nothing extra in most apps — the plugin manifest already requests:

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`
- `NEARBY_WIFI_DEVICES` (Android 13+)
- Wi-Fi and network state permissions

Location must be **on**. On Android 10+ the OS shows a system dialog to join
the network (`WifiNetworkSpecifier`). That join is app-scoped and may not
appear as a saved system network.

### iOS

In Xcode, enable:

1. **Hotspot Configuration**
2. **Access Wi-Fi Information**
3. Link `NetworkExtension.framework`

And in `Info.plist`:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>Location is used to read the current Wi-Fi name and join a network.</string>
```

Apple does not allow scanning nearby Wi-Fi. `getWifiList()` returns SSIDs this
app has configured, not every hotspot in range.

## Status values

| Value | Meaning |
| --- | --- |
| `connected` | Joined the requested SSID |
| `alreadyConnected` | Already on that SSID |
| `notConnected` | Join failed or was cancelled |
| `platformNotSupported` | OS version cannot configure Wi-Fi |
| `profileAlreadyInstalled` | iOS already has a profile for this SSID |
| `locationNotAllowed` | Location permission or services denied |

## Example

See the `example/` app for a small UI that lists networks, shows the current
SSID, and connects with a passphrase.
