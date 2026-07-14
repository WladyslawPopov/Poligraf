import Foundation
import SharedLogic

class IosResourceProvider: ResourceProvider {
    func getString(key: String) -> String {
        return NSLocalizedString(key, comment: "")
    }
    
    func getColorHex(token: ColorToken) -> String {
        switch token {
        case .background: return "#121212" // Same as Android
        case .surface: return "#1E1E1E"
        case .surfaceVariant: return "#2C2C2C"
        case .glassBase: return "#1AFFFFFF"
        case .glassBorder: return "#33FFFFFF"
        case .primary: return "#D1D1D1"
        case .onPrimary: return "#000000"
        case .truth: return "#00E676"
        case .stress: return "#FF5252"
        case .textPrimary: return "#FFFFFF"
        case .textSecondary: return "#8E8E93"
        case .textInverted: return "#000000"
        default: return "#000000"
        }
    }
    
    func getDimension(token: DimenToken) -> Float {
        switch token {
        case .mainPadding: return 16.0
        case .widgetSpacing: return 12.0
        case .cornerRadius: return 12.0
        case .iconSizeNav: return 24.0
        case .headerHeight: return 64.0
        default: return 0.0
        }
    }
    
    func getSystemIcon(key: String) -> String {
        switch key {
        case "mic": return "mic.fill"
        case "history": return "clock.fill"
        case "settings": return "gearshape.fill"
        case "profile": return "person.crop.circle.fill"
        case "chevron_right": return "chevron.right"
        case "menu": return "line.3.horizontal"
        default: return "questionmark.circle"
        }
    }
}
