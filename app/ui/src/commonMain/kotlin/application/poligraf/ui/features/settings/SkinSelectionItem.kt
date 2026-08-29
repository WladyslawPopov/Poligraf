package application.poligraf.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.ui.components.icons.AppIcon
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

    val borderColor = if (isSelected) {
        designSystem.color(ColorToken.ACCENT_PRIMARY)
    } else {
        designSystem.color(ColorToken.SURFACE_VARIANT).copy(alpha = 0.3f)
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (isSelected) designSystem.color(ColorToken.SURFACE_PRIMARY) else Color.Transparent)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
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
                        .border(2.dp, designSystem.color(ColorToken.SURFACE_PRIMARY), CircleShape),
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
