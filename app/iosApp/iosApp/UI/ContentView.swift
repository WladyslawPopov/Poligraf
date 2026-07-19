import SwiftUI
import SharedLogic

struct ContentView: View {
    @StateObject var root = RootComponentWrapper()

    var body: some View {
        NavigationStack(path: $root.path) {
            // Initial screen
            screen(for: AppRoute.Main())
                .navigationDestination(for: AppRoute.self) { route in
                    screen(for: route)
                }
        }
        .background(IosTheme.color(.background, from: root.designSystem).ignoresSafeArea())
        .sheet(isPresented: $root.isDrawerOpen) {
            DrawerView(root: root, designSystem: root.designSystem, onUserClose: {
                root.setDrawerOpen(isOpen: false)
            })
            .environment(\.colorScheme, root.isDark ? .dark : .light)
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
            .presentationBackground {
                IosTheme.color(.surface, from: root.designSystem)
                    .opacity(0.8)
                    .background(.ultraThinMaterial)
            }
        }
        .environment(\.colorScheme, root.isDark ? .dark : .light)
        .tint(IosTheme.color(.accentEnergy, from: root.designSystem))
    }
    
    @ViewBuilder
    private func screen(for route: AppRoute) -> some View {
        switch route {
        case is AppRoute.Main:
            MainView(root: root, component: root.getMainComponent(), designSystem: root.designSystem)
        case is AppRoute.Debug:
            DebugView(component: root.getDebugComponent(), designSystem: root.designSystem)
        default:
            EmptyView()
        }
    }
}
