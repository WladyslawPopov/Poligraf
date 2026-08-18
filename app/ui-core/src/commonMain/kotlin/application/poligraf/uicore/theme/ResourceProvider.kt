package application.poligraf.uicore.theme

import application.poligraf.uicore.theme.tokens.*

/**
 * Interface that platforms must implement to provide real native resources.
 */
interface ResourceProvider {
    
    fun getColorHex(token: ColorToken, isDark: Boolean = true): String {
        return ThemeDefaults.getColorHex(token, isDark)
    }
    
    fun getDimension(token: DimenToken): Float {
        return ThemeDefaults.getDimension(token)
    }
}
