import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate


    var body: some Scene {
        WindowGroup {
            ZStack {
                if let rootController = appDelegate.rootController {
                    ContentView(rootController: rootController)
                        .ignoresSafeArea(edges: .all)
                        .ignoresSafeArea(.keyboard)
                } else {
                    Color.black.ignoresSafeArea()
                }
            }
        }
    }
}
