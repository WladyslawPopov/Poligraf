import SwiftUI
import SharedLogic

struct InvestigationView: View {
    let component: InvestigationComponent
    let designSystem: DesignSystem
    
    @ObservedObject var state: SKIEStateObserver<InvestigationState>
    
    // Bridging common states
    @ObservedObject var loading: SKIEStateObserver<KotlinBoolean>
    @ObservedObject var error: SKIEOptionalStateObserver<ErrorType>
    @ObservedObject var toast: SKIEOptionalStateObserver<ToastState>

    init(component: InvestigationComponent, designSystem: DesignSystem) {
        self.component = component
        self.designSystem = designSystem
        self.state = SKIEStateObserver(component.viewModel.state)
        
        self.loading = SKIEStateObserver(component.viewModel.isLoading)
        self.error = SKIEOptionalStateObserver(component.viewModel.errorType)
        self.toast = SKIEOptionalStateObserver(component.viewModel.toastState)
    }

    var body: some View {
        AppScaffold(
            isLoading: loading.value.boolValue,
            errorType: error.value,
            toastState: toast.value,
            designSystem: designSystem,
            onClearError: { component.viewModel.clearError() },
            onClearToast: { component.viewModel.clearToast() }
        ) {
            VStack {
                Text("\(designSystem.string(token: .investigationScreenPlaceholder)): \(component.subjectId)")
                    .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
            }
            .navigationTitle(designSystem.string(token: .startInvestigation))
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
