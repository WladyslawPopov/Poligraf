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
                ContentView()
                    .ignoresSafeArea(edges: .all)
                    .ignoresSafeArea(.keyboard)
            }
        }
    }
}
