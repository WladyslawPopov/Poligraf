import SwiftUI
import SharedLogic

struct WidgetView: View {
    let widget: UiWidget
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    var body: some View {
        Group {
            if let welcome = widget as? UiWidget.WelcomeText {
                WelcomeTextView(widget: welcome, designSystem: designSystem)
            } else if let slider = widget as? UiWidget.SubjectSlider {
                SubjectSliderView(widget: slider, designSystem: designSystem, onAction: onAction)
            } else {
                EmptyView()
            }
        }
    }
}
