package application.poligraf.engine.component

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@OptIn(ExperimentalDecomposeApi::class)
interface AppComponentContext : JetpackComponentContext {
    val componentScope: CoroutineScope
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultAppComponentContext(
    private val componentContext: JetpackComponentContext
) : AppComponentContext, JetpackComponentContext by componentContext {

    override val componentScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        lifecycle.subscribe(object : Lifecycle.Callbacks {
            override fun onDestroy() {
                componentScope.cancel()
            }
        })
    }
}


@OptIn(ExperimentalDecomposeApi::class)
fun JetpackComponentContext.asAppComponentContext(): AppComponentContext =
    DefaultAppComponentContext(this)
