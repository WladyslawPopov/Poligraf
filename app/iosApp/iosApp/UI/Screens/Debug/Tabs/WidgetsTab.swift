import SwiftUI
import SharedLogic

struct WidgetsTab: View {
    let widgets: [UiWidget]
    let component: DebugComponent
    let designSystem: DesignSystem
    
    var body: some View {
        ScrollView {
            VStack {
                ForEach(widgets, id: \.id) { widget in
                    WidgetView(widget: widget, designSystem: designSystem, onAction: { component.onAction(action: $0) })
                }
            }
        }
    }
}
