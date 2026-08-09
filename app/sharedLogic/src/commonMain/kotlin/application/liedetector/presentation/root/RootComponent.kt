package application.liedetector.presentation.root

import androidx.compose.runtime.Stable
import application.liedetector.data.user.UserRepository
import application.liedetector.data.subject.SubjectRepository
import application.liedetector.engine.device.DeviceInfoProvider
import application.liedetector.engine.config.AppConfig
import application.liedetector.engine.navigation.AppNavigation
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.main.MainViewModel
import application.liedetector.presentation.debug.DebugComponent
import application.liedetector.presentation.debug.DebugViewModel
import application.liedetector.presentation.recording.RecordingComponent
import application.liedetector.presentation.recording.RecordingViewModel
import application.liedetector.presentation.recordingHistory.RecordingsHistoryComponent
import application.liedetector.presentation.recordingHistory.RecordingsHistoryViewModel
import application.liedetector.engine.component.ComponentContext
import application.liedetector.engine.io.audio.AudioRecorder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
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

    /**
     * Creates or retrieves a MainComponent.
     */
    fun mainComponent(screenContext: ComponentContext): MainComponent = 
        screenContext.instanceKeeper.getOrCreate("main") {
            MainComponent(screenContext, MainViewModel(subjectRepository, appConfig, navigation))
        }

    /**
     * Creates or retrieves a DebugComponent.
     */
    fun debugComponent(screenContext: ComponentContext): DebugComponent = 
        screenContext.instanceKeeper.getOrCreate("debug") {
            DebugComponent(screenContext, DebugViewModel(navigation))
        }

    /**
     * Creates or retrieves a RecordingComponent for a specific subject.
     * The instance is retained by the [screenContext.instanceKeeper] until the context is destroyed.
     */
    fun recordingComponent(screenContext: ComponentContext, subjectId: String): RecordingComponent = 
        screenContext.instanceKeeper.getOrCreate("recording_$subjectId") {
            RecordingComponent(
                context = screenContext,
                viewModel = RecordingViewModel(subjectId, navigation, subjectRepository, audioRecorder)
            )
        }

    /**
     * Creates or retrieves a RecordingsHistoryComponent for a specific subject.
     */
    fun recordingsHistoryComponent(screenContext: ComponentContext, subjectId: String, startRecording: Boolean = false): RecordingsHistoryComponent = 
        screenContext.instanceKeeper.getOrCreate("recordings_history_$subjectId") {
            RecordingsHistoryComponent(
                context = screenContext,
                viewModel = RecordingsHistoryViewModel(subjectId, navigation, subjectRepository, audioRecorder, startRecording)
            )
        }

    fun onDestroy() {
        // No manual children management needed, they are tied to their contexts
    }
}
