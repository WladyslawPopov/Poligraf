import SwiftUI
import SharedLogic

struct ContentView: View {
    var body: some View {
        ComposeView(
            root: AppCoordinator.shared.root,
            navigator: AppCoordinator.shared.navigator
        )
        .ignoresSafeArea(.all)
        .ignoresSafeArea(.keyboard)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let root: RootComponent
    let navigator: SharedNavigator

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(root: root, navigator: navigator)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
