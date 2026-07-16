import Foundation
import SharedLogic

class IosResourceProvider: ResourceProvider {
    func getString(key: String) -> String {
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
