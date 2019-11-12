import Flutter
import UIKit
import NetworkExtension
import SystemConfiguration.CaptiveNetwork

enum WifiStatus: String {
    case connected = "connected"
    case alreadyConnected = "alreadyConnected"
    case notConnected = "notConnected"
    case platformNotSupported = "platformNotSupported"
    case profileAlreadyInstalled = "profileAlreadyInstalled"
    
    func stringValue() -> String {
        return self.rawValue
    }
}


public class SwiftWifiConfigurationPlugin: NSObject, FlutterPlugin {
    
    
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "wifi_configuration", binaryMessenger: registrar.messenger())
        let instance = SwiftWifiConfigurationPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if "connectToWifi" == call.method {
            var map = call.arguments as? Dictionary<String, String>
            self.connectToWifi(result: result, ssid: map?["ssid"] ?? "", password: map?["password"] ?? "");
        } else if "getWifiList" == call.method {
            self.getWifiList(result: result);
        } else if "isConnectedToWifi" == call.method {
            var map = call.arguments as? Dictionary<String, String>
            result(self.isConnectedToCorrectWifi(wifiToCompareWith: map?["ssid"] ?? ""));
        } else if "connectedToWifi" == call.method {
            result(self.connectedToWifi());
        }
    }
    
    
    
    
    public func connectToWifi(result: @escaping FlutterResult, ssid: String, password:String) {
        print("ssid : \(ssid) \n pass: \(password)")
        if #available(iOS 11.0, *) {
            let configuration = NEHotspotConfiguration(ssid: ssid, passphrase: password, isWEP: false)
            NEHotspotConfigurationManager.shared.removeConfiguration(forSSID: ssid)
            
            NEHotspotConfigurationManager.shared.apply(configuration) { (error) in
                print("error in connection \(error)")
                if error != nil {
                    if error?.localizedDescription == "already associated." {
                        result(WifiStatus.alreadyConnected.stringValue());
                    } else {
                        if (error! as NSError).code == 10 {
                            //TODO: Add alert which will show the instructions to delete the installed profile
                            result(WifiStatus.profileAlreadyInstalled)
                        }
                        result(WifiStatus.notConnected.stringValue())
                    }
                }
                else {
                    print("ssid to check with \(ssid)")
                    
                    if self.isConnectedToCorrectWifi(wifiToCompareWith: ssid) {
                        print("connected to correct ssid")
                        result(WifiStatus.connected.stringValue())
                    } else {
                        print("not connected to correct ssid")
                        result(WifiStatus.notConnected.stringValue())
                    }
                    
                    
                }
                
            }
        } else {
            result(WifiStatus.notConnected.stringValue())
        }
    }
    
    
    
    public func getWifiList(result: @escaping FlutterResult) {
        var arrWifiSSID = [String]();
        if #available(iOS 11.0, *) {
            NEHotspotConfigurationManager.shared.getConfiguredSSIDs(completionHandler: { (arrConfiguredSSID) in
                arrWifiSSID  = arrConfiguredSSID
                print("list of available wifi is : ", arrWifiSSID)
                result(arrWifiSSID);
            })
        }
    }
    
    
    
    public func getWiFiSSID() -> String? {
        var ssid: String?
        if let interfaces = CNCopySupportedInterfaces() as NSArray? {
            for interface in interfaces {
                if let interfaceInfo = CNCopyCurrentNetworkInfo(interface as! CFString) as NSDictionary? {
                    ssid = interfaceInfo[kCNNetworkInfoKeySSID as String] as? String
                    break
                }
            }
        }
        return ssid
    }
    
    
    public func isConnectedToCorrectWifi(wifiToCompareWith wifi:String) -> Bool{
        
        if let deviceCurrentSSID = self.getWiFiSSID() {
            print("is connected to wifi \(wifi) and \(deviceCurrentSSID)")
            return (wifi == deviceCurrentSSID)
        } else {
            return false
        }
    }
    
    public func connectedToWifi() -> String{
        
        if let deviceCurrentSSID = self.getWiFiSSID() {
            print("is connected to wifi and \(deviceCurrentSSID)")
            return deviceCurrentSSID
        } else {
            return ""
        }
    }
    
}
