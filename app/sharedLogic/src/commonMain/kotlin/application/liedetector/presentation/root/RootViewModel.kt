package application.liedetector.presentation.root

import application.liedetector.data.user.UserRepository
import application.liedetector.presentation.base.BaseViewModel

class RootViewModel(private val userRepository: UserRepository) : BaseViewModel() {

    init {
        initializeApp()
    }

    private fun initializeApp() {
        launchSafe(
            block = {
                // 1. Ensure user is authorized anonymously
                userRepository.loginAnonymously()
            }
        )
    }
}
