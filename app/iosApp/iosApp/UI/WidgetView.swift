import SwiftUI
import SharedLogic

struct WidgetView: View {
    let widget: UiWidget
    let designSystem: DesignSystem
    let onAction: (WidgetAction) -> Void
    
    private var liquidGlassBackground: some View {
        Rectangle()
            .fill(.ultraThinMaterial)
            .clipShape(RoundedRectangle(cornerRadius: CGFloat(designSystem.dimen(token: .widgetCorner)), style: .continuous))
    }
    
    var body: some View {
        Group {
            if let header = widget as? UiWidget.Header {
                VStack(alignment: .center, spacing: CGFloat(designSystem.dimen(token: .spacingSmall))) {
                    Text(designSystem.string(token: header.titleToken))
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                        .multilineTextAlignment(.center)
                    
                    if let subtitleToken = header.subtitleToken {
                        Text(designSystem.string(token: subtitleToken))
                            .font(.subheadline)
                            .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                            .multilineTextAlignment(.center)
                    }
                }
                .padding(CGFloat(designSystem.dimen(token: .spacingLarge)))
                .frame(maxWidth: .infinity)
                .background(liquidGlassBackground)
                .overlay(
                    RoundedRectangle(cornerRadius: CGFloat(designSystem.dimen(token: .widgetCorner)), style: .continuous)
                        .stroke(IosTheme.color(.glassBorder, from: designSystem).opacity(0.5), lineWidth: 0.5)
                )
                .padding(.horizontal, CGFloat(designSystem.dimen(token: .spacingMedium)))
                .padding(.vertical, CGFloat(designSystem.dimen(token: .spacingSmall)))
                
            } else if let verdict = widget as? UiWidget.VerdictCard {
                HStack(spacing: CGFloat(designSystem.dimen(token: .spacingLarge))) {
                    VStack(alignment: .leading, spacing: CGFloat(designSystem.dimen(token: .spacingTiny))) {
                        Text(designSystem.string(key: verdict.verdictKey))
                            .font(.headline)
                            .foregroundColor(IosTheme.color(verdict.colorToken, from: designSystem))
                        
                        Text("ANALYSIS_SCORE")
                            .font(.caption)
                            .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
                    }
                    
                    Spacer()
                    
                    ZStack {
                        Circle()
                            .stroke(IosTheme.color(.glassBorder, from: designSystem).opacity(0.5), lineWidth: 4)
                        Circle()
                            .trim(from: 0, to: CGFloat(verdict.score) / 100.0)
                            .stroke(IosTheme.color(verdict.colorToken, from: designSystem), style: StrokeStyle(lineWidth: 4, lineCap: .round))
                            .rotationEffect(.degrees(-90))
                        
                        Text("\(verdict.score)%")
                            .font(.system(size: 14, weight: .bold, design: .monospaced))
                            .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                    }
                    .frame(width: 50, height: 50)
                }
                .padding(CGFloat(designSystem.dimen(token: .spacingMedium)))
                .background(liquidGlassBackground)
                .overlay(
                    RoundedRectangle(cornerRadius: CGFloat(designSystem.dimen(token: .widgetCorner)), style: .continuous)
                        .stroke(IosTheme.color(verdict.colorToken, from: designSystem).opacity(0.2), lineWidth: 0.5)
                )
                .padding(.horizontal, CGFloat(designSystem.dimen(token: .spacingMedium)))
                .padding(.vertical, CGFloat(designSystem.dimen(token: .spacingSmall)))

            } else if let graph = widget as? UiWidget.AcousticGraph {
                VStack(alignment: .leading, spacing: CGFloat(designSystem.dimen(token: .spacingSmall))) {
                    GeometryReader { geo in
                        SwiftUI.Path { path in
                            let width = geo.size.width
                            let height = geo.size.height
                            let step = width / CGFloat(max(1, graph.points.count - 1))
                            
                            for (index, point) in graph.points.enumerated() {
                                let x = CGFloat(index) * step
                                let y = height - (CGFloat(truncating: point as NSNumber) * height)
                                
                                if index == 0 {
                                    path.move(to: CGPoint(x: x, y: y))
                                } else {
                                    path.addLine(to: CGPoint(x: x, y: y))
                                }
                            }
                        }
                        .stroke(IosTheme.color(graph.colorToken, from: designSystem), lineWidth: 2)
                    }
                    .frame(height: 60)
                }
                .padding(CGFloat(designSystem.dimen(token: .spacingMedium)))
                .background(liquidGlassBackground)
                .padding(.horizontal, CGFloat(designSystem.dimen(token: .spacingMedium)))
                .padding(.vertical, CGFloat(designSystem.dimen(token: .spacingSmall)))

            } else if let micBtn = widget as? UiWidget.MicrophoneButton {
                Button(action: { onAction(micBtn.action) }) {
                    ZStack {
                        Circle()
                            .fill(.ultraThinMaterial)
                            .frame(width: 96, height: 96)
                            .overlay(Circle().stroke(IosTheme.color(.glassBorder, from: designSystem).opacity(0.5), lineWidth: 0.5))
                        
                        Image(systemName: "mic.fill")
                            .font(.system(size: 38))
                            .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                    }
                }
                .padding(.vertical, CGFloat(designSystem.dimen(token: .spacingLarge)))
                
            } else if let stdBtn = widget as? UiWidget.StandardButton {
                Button(action: { onAction(stdBtn.action) }) {
                    Text(designSystem.string(token: stdBtn.textToken))
                        .font(.headline)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, CGFloat(designSystem.dimen(token: .spacingMedium)))
                        .background(liquidGlassBackground)
                        .foregroundColor(IosTheme.color(.textPrimary, from: designSystem))
                        .overlay(
                            RoundedRectangle(cornerRadius: CGFloat(designSystem.dimen(token: .widgetCorner)), style: .continuous)
                                .stroke(IosTheme.color(.glassBorder, from: designSystem).opacity(0.5), lineWidth: 0.5)
                        )
                }
                .padding(.horizontal, CGFloat(designSystem.dimen(token: .spacingMedium)))
                .padding(.vertical, CGFloat(designSystem.dimen(token: .spacingSmall)))

            } else {
                Text("Unknown widget")
                    .foregroundColor(IosTheme.color(.textSecondary, from: designSystem))
            }
        }
    }
}
