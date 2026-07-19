import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        AppCoordinator.shared.setAppDelegate(appDelegate)
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                Color.black.ignoresSafeArea() // Hardware-level safety background
                
                ContentView()
                    .ignoresSafeArea(edges: .all)
                    .ignoresSafeArea(.keyboard)
            }
        }
    }
}
