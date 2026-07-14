package application.liedetector.uicore.theme

/**
 * Interface that platforms must implement to provide real native resources.
 */
interface ResourceProvider {
    fun getString(key: String): String
    
    // We'll use Hex strings or platform specific types in the actual implementation
    fun getColorHex(token: ColorToken): String
    
    fun getDimension(token: DimenToken): Float

    /**
     * Returns the platform-specific icon resource.
     */
    fun getSystemIcon(key: String): IconResource
}
