import SwiftUI
import SharedLogic

/**
 * Main UI Host for iOS.
 * Encapsulates NavigationStack and the mapping between routes and views.
 */
struct AppHost: View {
    let root: RootComponent
    let navigator: IosNavigator
    let designSystem: DesignSystem
    
    var body: some View {
        NavigationStack(path: Binding(
            get: { navigator.path },
            set: { navigator.path = $0 }
        )) {
            AdaptiveScreenHost(route: AppRoute.Main(), root: root, navigator: navigator, designSystem: designSystem)
                .navigationDestination(for: AppRoute.self) { route in
                    AdaptiveScreenHost(route: route, root: root, navigator: navigator, designSystem: designSystem)
                }
        }
        .background(designSystem.color(.background).ignoresSafeArea())
        .environment(\.colorScheme, navigator.isDark ? .dark : .light)
        .tint(designSystem.color(.accentEnergy))
    }
}
