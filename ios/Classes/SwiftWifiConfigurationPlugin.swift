import Flutter
import UIKit
import NetworkExtension
import SystemConfiguration.CaptiveNetwork

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
                        result(false);
                    } else {
                        if (error! as NSError).code == 10 {
                            //TODO: Add alert which will show the instructions to delete the installed profile
                            result(false)
                        }
                        result(false)
                    }
                }
                else {
                    DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 10.0) {
                        
                        if self.isConnectedToCorrectWifi(wifiToCompareWith: ssid) {
                            print("connected to correct ssid")
                            result(true)
                        } else {
                            print("not connected to correct ssid")
                            result(false)
                        }
                        
                    }
                }
                
            }
        } else {
            result(false)
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
            return (wifi == deviceCurrentSSID)
        } else {
            return false
        }
    }
    
}
