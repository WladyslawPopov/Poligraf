package application.liedetector.uicore.theme

/**
 * Interface that platforms must implement to provide real native resources.
 */
interface ResourceProvider {
    fun getString(token: StringToken): String
    
    fun getColorHex(token: ColorToken, isDark: Boolean = true): String {
        return ThemeDefaults.getColorHex(token, isDark)
    }
    
    fun getDimension(token: DimenToken): Float {
        return ThemeDefaults.getDimension(token)
    }

    /**
     * Returns the platform-specific icon resource.
     */
    fun getSystemIcon(key: String): IconResource
}
