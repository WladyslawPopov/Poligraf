import SwiftUI
import SharedLogic

struct WelcomeTextView: View {
    let widget: UiWidget.WelcomeText
    let designSystem: DesignSystem
    
    @Environment(\.verticalSizeClass) var verticalSizeClass
    
    var body: some View {
        let minHeight = verticalSizeClass == .compact 
            ? designSystem.dimen(.welcomeMinHeight) / 2.5
            : designSystem.dimen(.welcomeMinHeight)
            
        VStack(alignment: .leading) {
            TypingTextView(
                text: designSystem.string(token: widget.textToken) + (widget.emoji ?? ""),
                color: designSystem.color(widget.colorToken),
                font: designSystem.font(widget.typographyToken),
                typingDelay: Double(truncating: widget.typingDelay as NSNumber) / 1000.0
            )
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(designSystem.dimen(.spacingLarge))
        .frame(minHeight: minHeight, alignment: .center)
    }
}

struct TypingTextView: View {
    let text: String
    let color: Color
    let font: Font
    let typingDelay: Double
    
    @State private var displayedText: String = ""
    
    var body: some View {
        // Ghost Text Technique: Use ZStack to reserve space with invisible full text
        ZStack(alignment: .leading) {
            // 1. Ghost Layer: Invisible but reserves full space to prevent line jumps
            Text(text)
                .font(font)
                .opacity(0)
            
            // 2. Animated Layer: Visible typed text
            Text(displayedText)
                .font(font)
                .foregroundColor(color)
        }
        .task(id: text) {
            displayedText = ""
            let chars = Array(text)
            for char in chars {
                displayedText.append(char)
                try? await Task.sleep(nanoseconds: UInt64(typingDelay * 1_000_000_000))
            }
        }
    }
}
