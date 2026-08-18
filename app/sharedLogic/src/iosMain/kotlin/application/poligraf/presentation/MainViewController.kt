package application.poligraf.presentation

import androidx.compose.ui.window.ComposeUIViewController
import application.poligraf.navigation.SharedNavigator
import application.poligraf.presentation.root.RootComponent
import platform.UIKit.UIViewController

fun MainViewController(
    root: RootComponent,
    navigator: SharedNavigator
): UIViewController = ComposeUIViewController {
    App(root = root, navigator = navigator)
}
