#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint wifi_configuration.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'wifi_configuration'
  s.version          = '2.0.1'
  s.summary          = 'Connect to Wi-Fi, read the current SSID, and list networks on Android and iOS.'
  s.description      = <<-DESC
Connect Android and iOS devices to a Wi-Fi network, read the current SSID, and list available networks.
                       DESC
  s.homepage         = 'https://github.com/RohitKumarMishra/wifi_configuration'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Rohit Mishra' => 'rohitkrmisra@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '13.0'
  s.frameworks = 'NetworkExtension'

  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  s.resource_bundles = {'wifi_configuration_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
