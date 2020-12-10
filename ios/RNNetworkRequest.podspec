
Pod::Spec.new do |s|
  s.name         = "RNNetworkRequest"
  s.version      = "1.0.1"
  s.summary      = "RNNetworkRequest"
  s.description  = <<-DESC
                  RNNetworkRequest
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "alex.evans1230@gmail.com" }
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/Blitz-Mobile-Apps/react-native-network-request.git", :tag => "master" }
  s.source_files  = "RNNetworkRequest/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  