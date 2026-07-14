import UIKit
import FirebaseCore
import SharedLogic

class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        // 1. Setup Firebase
        FirebaseApp.configure()
        
        // 2. Setup Koin using Kotlin Factories
        let settings = IosSettingsFactoryKt.createIosSettings()
        let backgroundVisualizer = IosSettingsFactoryKt.createIosBackgroundVisualizer()
        
        InitKoinIosKt.doInitKoinIos(
            authService: IosAuthService(),
            analytics: IosAnalytics(),
            integrity: IosIntegrityImpl(),
            reviewManager: IosReviewManagerImpl(),
            resourceProvider: IosResourceProvider(),
            backgroundVisualizer: backgroundVisualizer,
            driverFactory: DriverFactory(),
            settings: settings
        )
        
        return true
    }
}
