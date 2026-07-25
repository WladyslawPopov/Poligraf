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
        
        InitKoinIosKt.doInitKoinIos(
            authService: IosAuthService(),
            analytics: IosAnalytics(),
            integrity: IosIntegrityImpl(),
            reviewManager: IosReviewManagerImpl(),
            resourceProvider: IosResourceProvider(),
            driverFactory: DriverFactory(),
            settings: settings,
            isDebug: isDebug
        )
        
        return true
    }
}
