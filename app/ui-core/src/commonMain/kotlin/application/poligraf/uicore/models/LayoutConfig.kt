package application.poligraf.uicore.models

import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.types.ContentPaddingType

data class LayoutConfig(
    val maxContentWidth: DimenToken? = DimenToken.MAX_CONTENT_WIDTH,
    val isCentered: Boolean = true,
    val useEdgeToEdge: Boolean = true,
    val contentPaddingType: ContentPaddingType = ContentPaddingType.NORMAL
)
