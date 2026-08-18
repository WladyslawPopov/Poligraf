import Foundation
import SharedLogic

class IosResourceProvider: ResourceProvider {
    
    func getColorHex(token: ColorToken, isDark: Bool) -> String {
        return ThemeDefaults.shared.getColorHex(token: token, isDark: isDark)
    }
    
    func getDimension(token: DimenToken) -> Float {
        return ThemeDefaults.shared.getDimension(token: token)
    }
}
