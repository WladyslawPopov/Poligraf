import SwiftUI
import SharedLogic

struct ContentView: View {
    let rootController: RootAppController
    
    var body: some View {
        ComposeView(rootController: rootController)
        .ignoresSafeArea(.all)
        .ignoresSafeArea(.keyboard)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let rootController: RootAppController

    func makeUIViewController(context: Context) -> UIViewController {
        rootController.rootViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
