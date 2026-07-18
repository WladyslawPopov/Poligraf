import Foundation
import SharedLogic

class IosResourceProvider: ResourceProvider {
    func getString(token: StringToken) -> String {
        let key: String
        switch token {
        case .welcomeTitle: key = "welcome_title"
        case .welcomeSubtitle: key = "welcome_subtitle"
        case .startInvestigation: key = "start_investigation"
        case .drawerSettings: key = "drawer_settings"
        case .drawerDarkMode: key = "drawer_dark_mode"
default: key = ""
        }
        return NSLocalizedString(key, comment: "")
    }
    
    func getColorHex(token: ColorToken, isDark: Bool) -> String {
        return ThemeDefaults.shared.getColorHex(token: token, isDark: isDark)
    }
    
    func getDimension(token: DimenToken) -> Float {
        return ThemeDefaults.shared.getDimension(token: token)
    }
    
    func getSystemIcon(key: String) -> String {
        return key
    }
}
