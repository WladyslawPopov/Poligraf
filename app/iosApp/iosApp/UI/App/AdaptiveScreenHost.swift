import SwiftUI
import SharedLogic

/**
 * Helper that creates and retains a ComponentContext for each unique screen destination.
 */
struct AdaptiveScreenHost: View {
    let route: AppRoute
    let root: RootComponent
    let navigator: IosNavigator
    let designSystem: DesignSystem
    
    @StateObject private var contextHolder = IosContextHolder()
    
    var body: some View {
        Group {
            switch route {
            case is AppRoute.Main:
                MainView(
                    navigator: navigator, 
                    component: root.mainComponent(screenContext: contextHolder.context), 
                    designSystem: designSystem
                )
            case is AppRoute.Debug:
                DebugView(
                    navigator: navigator, 
                    component: root.debugComponent(screenContext: contextHolder.context), 
                    designSystem: designSystem
                )
            case let rec as AppRoute.Recording:
                RecordingView(
                    navigator: navigator,
                    component: root.recordingComponent(screenContext: contextHolder.context, subjectId: rec.subjectId),
                    designSystem: designSystem
                )
            case let history as AppRoute.RecordingsHistory:
                RecordingsHistoryView(
                    navigator: navigator,
                    component: root.recordingsHistoryComponent(screenContext: contextHolder.context, subjectId: history.subjectId, startRecording: history.startRecording),
                    designSystem: designSystem
                )
            default:
                EmptyView()
            }
        }
    }
}

/**
 * A container to hold and manage a ComponentContext's lifecycle on iOS.
 */
class IosContextHolder: ObservableObject {
    let context: ComponentContext
    init() { self.context = IosComponentFactoryKt.componentContext() }
    deinit { context.viewModelStore.clear() }
}
