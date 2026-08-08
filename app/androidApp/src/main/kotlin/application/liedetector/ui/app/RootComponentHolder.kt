package application.liedetector.ui.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import application.liedetector.engine.component.DefaultComponentContext
import application.liedetector.navigation.AndroidNavigator
import application.liedetector.presentation.root.RootComponent

class RootComponentHolder : ViewModel(), LifecycleOwner, SavedStateRegistryOwner {

    val navigator = AndroidNavigator()

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val savedStateController by lazy { SavedStateRegistryController.create(this) }
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    private val rootViewModelStore = ViewModelStore()
    val root: RootComponent

    init {
        savedStateController.performRestore(null)

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        val context = DefaultComponentContext(
            lifecycle = lifecycleRegistry,
            viewModelStore = rootViewModelStore,
            savedStateRegistry = savedStateRegistry
        )

        root = RootComponent(context, navigator)
    }

    override fun onCleared() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        root.onDestroy()
        rootViewModelStore.clear()
        super.onCleared()
    }
}
