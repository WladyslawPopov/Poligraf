package application.liedetector.component

import androidx.activity.ComponentActivity

fun ComponentActivity.componentContext(): ComponentContext {
    return DefaultComponentContext(
        lifecycle = this.lifecycle,
        viewModelStore = this.viewModelStore,
        savedStateRegistry = this.savedStateRegistry
    )
}
