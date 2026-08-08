package application.liedetector.presentation.root

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import application.liedetector.data.user.UserRepository
import application.liedetector.data.subject.SubjectRepository
import application.liedetector.engine.device.DeviceInfoProvider
import application.liedetector.engine.config.AppConfig
import application.liedetector.navigation.AppNavigation
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.main.MainViewModel
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.presentation.debug.DebugViewModel
import application.liedetector.presentation.recording.RecordingComponent
import application.liedetector.presentation.recording.RecordingViewModel
import application.liedetector.engine.component.ComponentContext
import application.liedetector.engine.io.audio.AudioRecorder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RootComponent(
    val context: ComponentContext,
    val navigation: AppNavigation
) : KoinComponent {
    
    private val userRepository: UserRepository by inject()
    private val subjectRepository: SubjectRepository by inject()
    private val deviceProvider: DeviceInfoProvider by inject()
    private val appConfig: AppConfig by inject()
    private val audioRecorder: AudioRecorder by inject()

    val viewModel = RootViewModel(userRepository, deviceProvider)

    // Child components owned by the Root (Tree structure)
    val mainComponent: MainComponent by lazy {
        MainComponent(context, MainViewModel(subjectRepository, appConfig, navigation))
    }

    val debugComponent: DebugComponent by lazy {
        DebugComponent(context, DebugViewModel(navigation))
    }

    fun createRecordingComponent(subjectId: String): RecordingComponent {
        return RecordingComponent(
            context = context,
            viewModel = RecordingViewModel(subjectId, navigation, subjectRepository, audioRecorder)
        )
    }

    fun onDestroy() {
        (context.lifecycle as? LifecycleRegistry)?.currentState = Lifecycle.State.DESTROYED
    }
}
