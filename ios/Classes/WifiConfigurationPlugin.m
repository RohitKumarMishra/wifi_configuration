#import "WifiConfigurationPlugin.h"
#import <wifi_configuration/wifi_configuration-Swift.h>

@implementation WifiConfigurationPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftWifiConfigurationPlugin registerWithRegistrar:registrar];
}
@end
