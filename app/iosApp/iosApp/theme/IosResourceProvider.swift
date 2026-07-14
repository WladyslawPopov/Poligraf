import Foundation
import SharedLogic

class IosResourceProvider: ResourceProvider {
    func getString(key: String) -> String {
        return NSLocalizedString(key, comment: "")
    }
    
    func getColorHex(token: ColorToken) -> String {
        switch token {
        case .background: return "#000000"
        case .surface: return "#1C1C1E" // Standard dark gray
        case .surfaceVariant: return "#2C2C2E"
        case .primary: return "#E5E5EA" // Silver
        case .onPrimary: return "#000000"
        case .truth: return "#30D158" // Neon Green
        case .stress: return "#FF453A" // Neon Red
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
