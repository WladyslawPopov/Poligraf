import SwiftUI
import SharedLogic

struct MainView: View {
    let component: MainComponent
    let designSystem: DesignSystem
    @ObservedObject var state: ObservableState<MainState>
    @ObservedObject var root: RootComponentWrapper

    init(component: MainComponent, designSystem: DesignSystem, root: RootComponentWrapper) {
        self.component = component
        self.designSystem = designSystem
        self.root = root
        self._state = ObservedObject(wrappedValue: ObservableState<MainState>(component.stateWatcher))
    }

    var body: some View {
        ZStack {
            ScrollView {
                LazyVStack(spacing: CGFloat(designSystem.dimen(token: .widgetSpacing))) {
                    if let error = state.value.error {
                        Text(error)
                            .foregroundColor(.red)
                            .padding(.top, 100)
                    } else if state.value.widgets.isEmpty {
                        ProgressView()
                            .padding(.top, 100)
                    } else {
                        Spacer().frame(height: CGFloat(designSystem.dimen(token: .spacingLarge)))
                        ForEach(state.value.widgets, id: \.id) { widget in
                            WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                        }
                    }
                }
            }
            .scrollContentBackground(.hidden)
        }
        .containerBackground(.clear, for: .navigation)
        .navigationTitle(state.value.topBarState.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { root.navigator.toggleDrawer() }) {
                    Image(systemName: "line.3.horizontal")
                }
            }
        }
    }
}
