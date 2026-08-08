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

        let context = IosComponentFactoryKt.componentContext()
        let root = RootComponent(context : context, navigation : nav)
        self.component = root
        
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
        .background(designSystem.color(.background).ignoresSafeArea())
        .environment(\.colorScheme, navigator.isDark ? .dark : .light)
        .tint(designSystem.color(.accentEnergy))
    }
    
    @ViewBuilder
    private func screen(for route: AppRoute) -> some View {
        switch route {
        case is AppRoute.Main:
            MainView(navigator: navigator, component: component.mainComponent, designSystem: designSystem)
        case is AppRoute.Debug:
            DebugView(navigator: navigator, component: component.debugComponent, designSystem: designSystem)
        case let rec as AppRoute.Recording:
            RecordingView(
                navigator: navigator,
                component: component.createRecordingComponent(subjectId: rec.subjectId),
                designSystem: designSystem
            )
        default:
            EmptyView()
        }
    }
}
