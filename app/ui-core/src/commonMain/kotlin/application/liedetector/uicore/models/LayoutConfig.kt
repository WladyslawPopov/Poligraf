package application.liedetector.uicore.models

import application.liedetector.uicore.theme.tokens.DimenToken
import application.liedetector.uicore.types.ContentPaddingType

data class LayoutConfig(
    val maxContentWidth: DimenToken? = DimenToken.MAX_CONTENT_WIDTH,
    val isCentered: Boolean = true,
    val useEdgeToEdge: Boolean = true,
    val contentPaddingType: ContentPaddingType = ContentPaddingType.NORMAL
)
