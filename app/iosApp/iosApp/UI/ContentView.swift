import SwiftUI
import SharedLogic

struct ContentView: View {
    @StateObject var root = RootComponentWrapper()

    var body: some View {
        IosNavHost(
            navigator: root.navigator,
            designSystem: root.designSystem
        ) { component in
            Group {
                if let main = component as? MainComponent {
                    MainView(component: main, designSystem: root.designSystem, root: root)
                } else {
                    Text("Loading...")
                }
            }
        } drawerView: {
            DrawerView(root: root, designSystem: root.designSystem)
        }
    }
}
