import UIKit
import FirebaseCore
import SharedLogic

class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        // 0. Setup Logging
        #if DEBUG
        Napier.shared.base(antilog: DebugAntilog(defaultTag: "Debug"))
        #endif

        // 1. Setup Firebase
        FirebaseApp.configure()
        
        // 2. Setup Koin using Kotlin Factories
        let settings = NSUserDefaultsSettings(delegate: UserDefaults.standard)
        
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        
        let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
        let deviceId = UIDevice.current.identifierForVendor?.uuidString ?? "unknown_ios"

        InitKoinIosKt.doInitKoinIos(
            authService: IosAuthService(),
            analytics: IosAnalytics(),
            integrity: IosIntegrityImpl(),
            reviewManager: IosReviewManagerImpl(),
            resourceProvider: IosResourceProvider(),
            driverFactory: DriverFactory(),
            settings: settings,
            appVersion: version,
            deviceId: deviceId,
            isDebug: isDebug
        )
        
        return true
    }
}
