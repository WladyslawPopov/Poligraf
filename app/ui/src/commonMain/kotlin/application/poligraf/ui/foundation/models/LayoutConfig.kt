package application.poligraf.ui.foundation.models

import application.poligraf.ui.theme.tokens.DimenToken
import application.poligraf.ui.foundation.state.ContentPaddingType

data class LayoutConfig(
    val maxContentWidth: DimenToken? = DimenToken.MAX_CONTENT_WIDTH,
    val isCentered: Boolean = true,
    val useEdgeToEdge: Boolean = true,
    val contentPaddingType: ContentPaddingType = ContentPaddingType.NORMAL
)
