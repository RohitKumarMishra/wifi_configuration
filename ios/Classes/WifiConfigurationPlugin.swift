import Flutter
import NetworkExtension
import UIKit

public class WifiConfigurationPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(
      name: "wifi_configuration",
      binaryMessenger: registrar.messenger()
    )
    let instance = WifiConfigurationPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? [String: Any]
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "connectToWifi":
      connectToWifi(
        ssid: arguments?["ssid"] as? String ?? "",
        password: arguments?["password"] as? String ?? "",
        result: result
      )
    case "getWifiList":
      getWifiList(result: result)
    case "isConnectedToWifi":
      let ssid = arguments?["ssid"] as? String ?? ""
      currentSsid { current in
        result(!ssid.isEmpty && current == ssid)
      }
    case "connectedToWifi":
      currentSsid { ssid in
        result(ssid)
      }
    default:
      result(FlutterMethodNotImplemented)
    }
  }

  private func connectToWifi(ssid: String, password: String, result: @escaping FlutterResult) {
    guard !ssid.isEmpty else {
      result("notConnected")
      return
    }
    guard #available(iOS 11.0, *) else {
      result("platformNotSupported")
      return
    }

    currentSsid { current in
      if current == ssid {
        result("alreadyConnected")
        return
      }

      let configuration: NEHotspotConfiguration
      if password.isEmpty {
        configuration = NEHotspotConfiguration(ssid: ssid)
      } else {
        configuration = NEHotspotConfiguration(ssid: ssid, passphrase: password, isWEP: false)
      }
      configuration.joinOnce = false

      NEHotspotConfigurationManager.shared.apply(configuration) { error in
        if let error = error as NSError? {
          let message = error.localizedDescription.lowercased()
          if message.contains("already associated") {
            result("alreadyConnected")
            return
          }
          // NEHotspotConfigurationError.alreadyAssociated / userDenied / pending
          if error.code == NEHotspotConfigurationError.alreadyAssociated.rawValue {
            result("alreadyConnected")
            return
          }
          if error.code == NEHotspotConfigurationError.userDenied.rawValue {
            result("notConnected")
            return
          }
          // Historical mapping from 1.x for an existing configuration profile.
          if error.code == 10 {
            result("profileAlreadyInstalled")
            return
          }
          result("notConnected")
          return
        }

        self.currentSsid { joined in
          result(joined == ssid ? "connected" : "notConnected")
        }
      }
    }
  }

  private func getWifiList(result: @escaping FlutterResult) {
    guard #available(iOS 11.0, *) else {
      result([String]())
      return
    }
    NEHotspotConfigurationManager.shared.getConfiguredSSIDs { ssids in
      result(ssids)
    }
  }

  private func currentSsid(completion: @escaping (String) -> Void) {
    if #available(iOS 14.0, *) {
      NEHotspotNetwork.fetchCurrent { network in
        completion(network?.ssid ?? "")
      }
      return
    }
    completion("")
  }
}
