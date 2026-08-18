import SwiftUI
import SharedLogic

/**
 * Extensions to DesignSystem to provide SwiftUI-friendly access to theme tokens.
 * We provide both unlabeled and labeled overloads to support various call styles
 * while ensuring they return native SwiftUI types (Color, CGFloat).
 * We call resources directly to avoid ambiguity with DesignSystem methods.
 */
extension DesignSystem {
    
    // MARK: - Color
    
    func color(_ token: ColorToken) -> Color {
        let hex = self.resources.getColorHex(token: token, isDark: self.isDark)
        return Color(hex: hex)
    }
    
    func color(token: ColorToken) -> Color {
        return self.color(token)
    }
    
    // MARK: - Dimensions
    
    func dimen(_ token: DimenToken) -> CGFloat {
        let val = self.resources.getDimension(token: token)
        return CGFloat(val)
    }
    
    func dimen(token: DimenToken) -> CGFloat {
        return self.dimen(token)
    }
    
    // MARK: - Typography
    
    func font(_ token: TypographyToken) -> Font {
        switch token {
        case .header: return .system(size: 34, weight: .bold)
        case .subheader: return .system(size: 24, weight: .semibold)
        case .body: return .system(size: 17, weight: .regular)
        case .caption: return .system(size: 12, weight: .regular)
        case .dataNumeric: return .system(size: 20, weight: .bold, design: .monospaced)
        }
    }
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }

    var components: (red: CGFloat, green: CGFloat, blue: CGFloat, opacity: CGFloat) {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var o: CGFloat = 0
        UIColor(self).getRed(&r, green: &g, blue: &b, alpha: &o)
        return (r, g, b, o)
    }
}
