package application.poligraf.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.domain.analyzer.types.AnalyzerSkin
import application.poligraf.ui.components.icons.AppIcon
import application.poligraf.ui.components.items.SelectableItem
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun SkinSelectionItem(
    skin: AnalyzerSkin,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val designSystem = LocalDesignSystem.current
    val icon = when (skin) {
        AnalyzerSkin.STATE_MAP -> IconToken.SKIN_TRIANGLE
        AnalyzerSkin.VOICE_RIBBON -> IconToken.SKIN_WAVE
        AnalyzerSkin.EQUALIZER -> IconToken.SKIN_BARS
        AnalyzerSkin.RINGS -> IconToken.SKIN_RINGS
    }

    val name = when (skin) {
        AnalyzerSkin.STATE_MAP -> designSystem.string(StringToken.SKIN_STATE_MAP)
        AnalyzerSkin.VOICE_RIBBON -> designSystem.string(StringToken.SKIN_VOICE_RIBBON)
        AnalyzerSkin.EQUALIZER -> designSystem.string(StringToken.SKIN_EQUALIZER)
        AnalyzerSkin.RINGS -> designSystem.string(StringToken.SKIN_RINGS)
    }

    SelectableItem(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier.width(90.dp),
        size = null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(designSystem.color(ColorToken.SURFACE_SECONDARY), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    icon = designSystem.icon(icon),
                    contentDescription = null,
                    tint = if (isSelected) designSystem.color(ColorToken.ACCENT_PRIMARY) else designSystem.color(
                        ColorToken.TEXT_SECONDARY
                    )
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(16.dp)
                            .background(designSystem.color(ColorToken.ACCENT_PRIMARY), CircleShape)
                            .border(
                                2.dp,
                                designSystem.color(ColorToken.SURFACE_PRIMARY),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            icon = designSystem.icon(IconToken.CHECK),
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) designSystem.color(ColorToken.TEXT_PRIMARY) else designSystem.color(
                    ColorToken.TEXT_SECONDARY
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
