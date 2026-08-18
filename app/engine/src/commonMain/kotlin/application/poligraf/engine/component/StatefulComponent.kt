package application.poligraf.engine.component

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update

interface StatefulComponent<M : Any> {
    val model: Value<M>
}

abstract class BaseComponent<M : Any>(
    componentContext: AppComponentContext,
    initialModel: M
) : AppComponentContext by componentContext, StatefulComponent<M> {

    private val _model = MutableValue(initialModel)
    override val model: Value<M> = _model

    protected fun updateModel(reducer: (M) -> M) {
        _model.update(reducer)
    }
}
