import SwiftUI
import SharedLogic

struct ContentView: View {
    @ObservedObject var navigator: IosNavigator = AppCoordinator.shared.navigator
    
    private var root: RootComponent { AppCoordinator.shared.root }

    private var designSystem: DesignSystem {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        return DesignSystem(resources: IosResourceProvider(), isDark: navigator.isDark, isDebug: isDebug)
    }

    var body: some View {
        AppHost(
            root: root,
            navigator: navigator,
            designSystem: designSystem
        )
    }
}
