require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-sns"
  s.version      = package['version'].gsub(/v|-beta/, '')
  s.summary      = package['description']
  s.author       = package['author']
  s.license      = package['license']
  s.homepage     = package['homepage']
  s.platform     = :ios, "8.0"
  s.source       = { :git => "https://github.com/daivdggdev/react-native-sns", :tag => "v#{s.version}" }
  s.source_files = "ios/RNSns/*.{h,m}"

  s.dependency 'React'
  s.dependency 'UMCommon'
  s.dependency 'UMDevice'
  # U-Share SDK UI模块（分享面板，建议添加）
  s.dependency 'UMShare/UI'
  # 集成微信(精简版0.2M)
  s.dependency 'UMShare/Social/WeChat'
  # 集成QQ(精简版0.5M)
  s.dependency 'UMShare/Social/QQ'
  # 集成新浪微博(精简版1M)
  s.dependency 'UMShare/Social/Sina'
end
