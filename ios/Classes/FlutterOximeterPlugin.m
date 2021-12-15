#import "FlutterOximeterPlugin.h"
#if __has_include(<flutter_oximeter/flutter_oximeter-Swift.h>)
#import <flutter_oximeter/flutter_oximeter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_oximeter-Swift.h"
#endif

@implementation FlutterOximeterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterOximeterPlugin registerWithRegistrar:registrar];
}
@end
