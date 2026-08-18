package application.poligraf.presentation.root

import androidx.compose.runtime.Stable
import application.poligraf.engine.device.DeviceInfoProvider
import application.poligraf.engine.config.AppConfig
import application.poligraf.engine.navigation.AppNavigation
import application.poligraf.presentation.main.MainComponent
import application.poligraf.presentation.main.MainViewModel
import application.poligraf.presentation.debug.DebugComponent
import application.poligraf.presentation.debug.DebugViewModel
import application.poligraf.presentation.recordingHistory.RecordingsHistoryComponent
import application.poligraf.presentation.recordingHistory.RecordingsHistoryViewModel
import application.poligraf.domain.usecase.recording.DeleteRecordingUseCase
import application.poligraf.domain.usecase.recording.GetRecordingsUseCase
import application.poligraf.domain.usecase.recording.LoadRecordingsUseCase
import application.poligraf.domain.usecase.recording.SaveRecordingUseCase
import application.poligraf.engine.component.ComponentContext
import application.poligraf.engine.io.audio.AudioRecorder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class RootComponent(
    val context: ComponentContext,
    val navigation: AppNavigation
) : KoinComponent {

    private val deviceProvider: DeviceInfoProvider by inject()
    private val appConfig: AppConfig by inject()
    private val audioRecorder: AudioRecorder by inject()
    
    private val getRecordingsUseCase: GetRecordingsUseCase by inject()
    private val saveRecordingUseCase: SaveRecordingUseCase by inject()
    private val deleteRecordingUseCase: DeleteRecordingUseCase by inject()
    private val loadRecordingsUseCase: LoadRecordingsUseCase by inject()

    val viewModel = RootViewModel(deviceProvider)

    /**
     * Creates or retrieves a MainComponent.
     */
    fun mainComponent(screenContext: ComponentContext): MainComponent = 
        screenContext.instanceKeeper.getOrCreate("main") {
            MainComponent(screenContext, MainViewModel(appConfig, navigation))
        }

    /**
     * Creates or retrieves a DebugComponent.
     */
    fun debugComponent(screenContext: ComponentContext): DebugComponent = 
        screenContext.instanceKeeper.getOrCreate("debug") {
            DebugComponent(screenContext, DebugViewModel(navigation))
        }


    /**
     * Creates or retrieves a RecordingsHistoryComponent for a specific subject.
     */
    fun recordingsHistoryComponent(screenContext: ComponentContext, subjectId: String, startRecording: Boolean = false): RecordingsHistoryComponent = 
        screenContext.instanceKeeper.getOrCreate("recordings_history_$subjectId") {
            RecordingsHistoryComponent(
                context = screenContext,
                viewModel = RecordingsHistoryViewModel(
                    subjectId = subjectId,
                    navigation = navigation,
                    audioRecorder = audioRecorder,
                    getRecordingsUseCase = getRecordingsUseCase,
                    saveRecordingUseCase = saveRecordingUseCase,
                    deleteRecordingUseCase = deleteRecordingUseCase,
                    loadRecordingsUseCase = loadRecordingsUseCase,
                    startRecording = startRecording
                )
            )
        }

    fun onDestroy() {
        // No manual children management needed, they are tied to their contexts
    }
}
