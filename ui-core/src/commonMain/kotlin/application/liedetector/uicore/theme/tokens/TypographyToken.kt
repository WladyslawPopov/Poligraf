package application.liedetector.uicore.theme.tokens

import kotlinx.serialization.Serializable

@Serializable
enum class TypographyToken {
    HEADER,
    SUBHEADER,
    BODY,
    CAPTION,
    DATA_NUMERIC // Specialized for percentages/scores
}
