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
            ScalesView(visualizer: backgroundState, designSystem: designSystem)
                .ignoresSafeArea()
            
            Rectangle()
                .fill(.thinMaterial) 
                .opacity(0.8)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                if let error = state.value.error {
                    // Show Honest Error
                    Spacer()
                    Text(error)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                        .padding(32)
                    Spacer()
                } else if state.value.widgets.isEmpty {
                    Spacer()
                    ProgressView().tint(IosTheme.color(.primary, from: designSystem)).scaleEffect(1.5)
                    Spacer()
                } else {
                    List {
                        ForEach(state.value.widgets, id: \.id) { widget in
                            WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                        }
                    }
                    .scrollContentBackground(.hidden)
                    .background(Color.clear)
                }
            }
        }
        .navigationTitle(state.value.topBarState.title)
        .navigationBarTitleDisplayMode(.inline)
    }
}
