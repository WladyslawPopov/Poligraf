import SwiftUI
import SharedLogic

struct InvestigationView: View {
    @ObservedObject var navigator: IosNavigator
    let component: InvestigationComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<InvestigationState>

    init(navigator: IosNavigator, component: InvestigationComponent, designSystem: DesignSystem) {
        self.navigator = navigator
        self.component = component
        self.designSystem = designSystem
        self._state = ObservedObject(wrappedValue: SKIEStateObserver(component.viewModel.state))
    }

    var body: some View {
        AppScaffold(
            viewModel: component.viewModel,
            state: state.value,
            designSystem: designSystem,
            onRetry: { },
            onRefresh: { }
        ) {
            VStack {
                // 1. Evidence List
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(state.value.widgets, id: \.id) { widget in
                            WidgetView(widget: widget, designSystem: designSystem, onAction: { _ in })
                        }
                    }
                    .padding()
                }
                
                Spacer()
                
                // 2. Control Panel Island
                HStack(spacing: 24) {
                    InvestigationActionButton(systemName: designSystem.icon(token: .mic), designSystem: designSystem)
                    InvestigationActionButton(systemName: designSystem.icon(token: .gallery), designSystem: designSystem)
                    InvestigationActionButton(systemName: designSystem.icon(token: .note), designSystem: designSystem)
                }
                .padding(8)
                .background(.ultraThinMaterial)
                .clipShape(Capsule())
                .padding(.bottom, 20)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { component.goBack() }) {
                    Image(systemName: designSystem.icon(token: .arrowBack))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                        .frame(width: 38, height: 38)
                        .clipShape(Circle())
                }
            }
            
            ToolbarItem(placement: .principal) {
                HStack(spacing: 8) {
                    Text(state.value.subject?.avatar ?? "🕵️")
                    Text(state.value.subject?.name ?? "Undefined-1")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .clipShape(Capsule())
            }
            
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { /* TODO: Open Subject Profile */ }) {
                    Image(systemName: designSystem.icon(token: .settings))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                        .frame(width: 38, height: 38)
                        .clipShape(Circle())
                }
            }
        }
    }
}

struct InvestigationActionButton: View {
    let systemName: String
    let designSystem: DesignSystem
    
    var body: some View {
        Button(action: { }) {
            Image(systemName: systemName)
                .font(.title3)
                .foregroundColor(IosTheme.color(.textInverted, from: designSystem))
                .frame(width: 52, height: 52)
                .background(IosTheme.color(.accentPrimary, from: designSystem))
                .clipShape(Circle())
        }
    }
}
