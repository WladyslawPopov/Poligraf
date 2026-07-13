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
     * Returns the name of the system icon (e.g. SF Symbol name or Material Icon name)
     */
    fun getSystemIconName(key: String): String
}
