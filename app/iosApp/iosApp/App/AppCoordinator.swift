import Foundation
import UIKit
import SharedLogic

class AppCoordinator: ObservableObject {
    static let shared = AppCoordinator()
    
    private var appDelegate: AppDelegate?
    
    let navigator: SharedNavigator
    var voiceRecorderEngine: NativeVoiceRecorderEngine?
    
    // Lazy initialization ensures Koin is started before RootComponent is created
    lazy var root: RootComponent = {
        let context = IosComponentFactoryKt.componentContext()
        return RootComponent(context: context, navigation: navigator)
    }()
    
    init() {
        self.navigator = SharedNavigator()
    }
    
    func setAppDelegate(_ delegate: AppDelegate) {
        self.appDelegate = delegate
    }
}
