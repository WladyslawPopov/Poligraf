import SwiftUI
import SharedLogic

struct ContentView: View {
    @StateObject var navigator = IosNavigator()
    
    // Manage RootComponent lifecycle directly
    private let component: RootComponent
    private let designSystem: DesignSystem

    init() {
        let nav = IosNavigator()
        let root = IosComponentFactoryKt.createRootComponent(navigation: nav)
        self.component = root
        
        // We use a temporary navigator for init, but it will be replaced by the @StateObject
        // In SwiftUI, init() is called multiple times, so we need to be careful with @StateObject
        self._navigator = StateObject(wrappedValue: nav)
        
        // Design system depends on navigator's theme state
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        self.designSystem = DesignSystem(resources: IosResourceProvider(), isDark: nav.isDark, isDebug: isDebug)
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
        case let route as AppRoute.Investigation:
            InvestigationView(component: component.createInvestigationComponent(subjectId: route.subjectId), designSystem: designSystem)
        default:
            EmptyView()
        }
    }
}
