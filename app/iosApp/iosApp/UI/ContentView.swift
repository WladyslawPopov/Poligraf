import SwiftUI
import SharedLogic

struct ContentView: View {
    @StateObject var root = RootComponentWrapper()

    var body: some View {
        ZStack {
            IosNavHost(navigator: root.navigator) { component in
                Group {
                    if let main = component as? MainComponent {
                        MainView(component: main, designSystem: root.designSystem)
                    } else {
                        Text("Loading...")
                    }
                }
            }
        }
    }
}
