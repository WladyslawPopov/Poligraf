import Foundation
import SharedLogic

class IosResourceProvider: ResourceProvider {
    func getString(key: String) -> String {
        return NSLocalizedString(key, comment: "")
    }
    
    func getColorHex(token: ColorToken) -> String {
        switch token {
        case .primary: return "#6200EE"
        case .secondary: return "#03DAC6"
        case .background: return "#FFFFFF"
        case .surface: return "#FFFFFF"
        case .error: return "#B00020"
        case .onPrimary: return "#FFFFFF"
        case .onBackground: return "#000000"
        case .textPrimary: return "#000000"
        case .textSecondary: return "#757575"
        case .accentStress: return "#FF5722"
        case .accentTruth: return "#4CAF50"
        default: return "#000000"
        }
    }
    
    func getDimension(token: DimenToken) -> Float {
        switch token {
        case .spacingSmall: return 8.0
        case .spacingMedium: return 16.0
        case .spacingLarge: return 24.0
        case .cornerRadius: return 12.0
        case .iconSizeSmall: return 24.0
        case .iconSizeMedium: return 32.0
        default: return 0.0
        }
    }
    
    func getSystemIconName(key: String) -> String {
        switch key {
        case "mic": return "mic.fill"
        case "history": return "clock.fill"
        case "settings": return "gearshape.fill"
        default: return "questionmark.circle"
        }
    }
}
