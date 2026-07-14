package application.liedetector.presentation.root

import androidx.compose.runtime.Stable
import application.liedetector.data.user.UserRepository
import application.liedetector.models.KmpResult
import application.liedetector.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
data class RootState(
    val isInitialized: Boolean = false,
    val error: String? = null
)

class RootViewModel(private val userRepository: UserRepository) : BaseViewModel() {
    private val _state = MutableStateFlow(RootState())
    val state: StateFlow<RootState> = _state.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        launchSafe(block = {
            // 1. Ensure user is authorized anonymously
            val authResult = userRepository.loginAnonymously()
            if (authResult is KmpResult.Success) {
                _state.value = _state.value.copy(isInitialized = true)
            } else if (authResult is KmpResult.Error) {
                _state.value = _state.value.copy(error = authResult.throwable.message)
            }
        })
    }
}
