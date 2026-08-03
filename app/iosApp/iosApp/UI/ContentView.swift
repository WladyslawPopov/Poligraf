import SwiftUI
import SharedLogic

struct ContentView: View {
    @StateObject var navigator = IosNavigator()
    
    // Manage RootComponent lifecycle directly
    private let component: RootComponent

    private var designSystem: DesignSystem {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        return DesignSystem(resources: IosResourceProvider(), isDark: navigator.isDark, isDebug: isDebug)
    }

    init() {
        let nav = IosNavigator()
        let root = IosComponentFactoryKt.createRootComponent(navigation: nav)
        self.component = root
        
        // We use a temporary navigator for init, but it will be replaced by the @StateObject
        // In SwiftUI, init() is called multiple times, so we need to be careful with @StateObject
        self._navigator = StateObject(wrappedValue: nav)
    }

    var body: some View {
        NavigationStack(path: $navigator.path) {
            // Initial screen
            screen(for: AppRoute.Main())
                .navigationDestination(for: AppRoute.self) { route in
                    screen(for: route)
                }
        }
        .background(IosTheme.color(.background, from: designSystem).ignoresSafeArea())
        .environment(\.colorScheme, navigator.isDark ? .dark : .light)
        .tint(IosTheme.color(.accentEnergy, from: designSystem))
    }
    
    @ViewBuilder
    private func screen(for route: AppRoute) -> some View {
        switch route {
        case is AppRoute.Main:
            MainView(navigator: navigator, component: component.mainComponent, designSystem: designSystem)
        case is AppRoute.Debug:
            DebugView(navigator: navigator, component: component.debugComponent, designSystem: designSystem)
        case let inv as AppRoute.Investigation:
            InvestigationView(
                navigator: navigator,
                component: component.createInvestigationComponent(subjectId: inv.subjectId),
                designSystem: designSystem
            )
        default:
            EmptyView()
        }
    }
}
