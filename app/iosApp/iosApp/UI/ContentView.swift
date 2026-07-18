import SwiftUI
import SharedLogic

struct ContentView: View {
    @StateObject var root = RootComponentWrapper()

    var body: some View {
        IosNavHost(
            navigator: root.navigator,
            designSystem: root.designSystem
        ) { component, navigator in
            Group {
                switch component {
                case let main as MainComponent:
                    MainView(component: main, designSystem: root.designSystem, navigator: navigator)
                default:
                    EmptyView()
                }
            }
        } drawerView: {
            DrawerView(root: root, designSystem: root.designSystem)
        }
    }
}
