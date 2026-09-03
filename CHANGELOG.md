## 2.0.0

* Dart 3 / null-safety rewrite of the 1.2.1 Android and iOS plugin.
* Android uses embedding v2, `WifiNetworkSpecifier` on API 29+, and no longer
  depends on Google Play Services.
* iOS connect uses `NEHotspotConfiguration`; current SSID uses
  `NEHotspotNetwork.fetchCurrent` (iOS 14+).
* MIT license (pub.dev previously reported the license as unknown).
* Breaking: requires Dart 3 and Flutter 3.10+.

## 1.2.1

* Location prompt on Android and `WifiConnectionStatus` enum.
