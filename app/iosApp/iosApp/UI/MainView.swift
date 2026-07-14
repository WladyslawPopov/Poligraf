import SwiftUI
import SharedLogic

struct MainView: View {
    let component: MainComponent
    let designSystem: DesignSystem
    @ObservedObject var state: ObservableState<MainState>
    @ObservedObject var backgroundState: ObservableState<BackgroundState>
    
    init(component: MainComponent, designSystem: DesignSystem) {
        self.component = component
        self.designSystem = designSystem
        self._state = ObservedObject(wrappedValue: ObservableState<MainState>(component.stateWatcher))
        self._backgroundState = ObservedObject(wrappedValue: ObservableState<BackgroundState>(component.backgroundWatcher))
    }
    
    var body: some View {
        ZStack {
            // 1. Concrete Foundation (Blurred)
            ScalesView(visualizer: backgroundState, designSystem: designSystem)
                .blur(radius: 4)
                .ignoresSafeArea()
            
            // 2. The Dark Veil (Separates BG from Content)
            Color.black.opacity(0.18)
                .ignoresSafeArea()
            
            // 3. Content
            ScrollView {
                LazyVStack(spacing: 12) {
                    if let error = state.value.error {
                        VStack(spacing: 16) {
                            Text(error).foregroundColor(.red).multilineTextAlignment(.center)
                            Button(action: { component.retry() }) {
                                Image(systemName: "arrow.clockwise.circle.fill").font(.largeTitle)
                            }
                        }.padding(.top, 100)
                    } else if state.value.widgets.isEmpty {
                        ProgressView().tint(.white).scaleEffect(1.5).padding(.top, 100)
                    } else {
                        Spacer().frame(height: 30)
                        ForEach(state.value.widgets, id: \.id) { widget in
                            WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                        }
                    }
                }
            }
        }
        .navigationTitle(state.value.topBarState.title)
        .navigationBarTitleDisplayMode(.inline)
        .preferredColorScheme(.dark) // Keep it dark as intended
    }
}
