package application.poligraf.ui.features.recorder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import application.poligraf.domain.model.AnalyzerSkin
import application.poligraf.ui.theme.LocalDesignSystem
import application.poligraf.ui.theme.tokens.ColorToken
import application.poligraf.ui.theme.tokens.IconToken
import application.poligraf.ui.theme.tokens.StringToken

@Composable
fun SkinSwitcher(
    currentSkin: AnalyzerSkin,
    onSkinChange: (AnalyzerSkin) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val designSystem = LocalDesignSystem.current

    val skinIcon = when (currentSkin) {
        AnalyzerSkin.STATE_MAP -> designSystem.icon(IconToken.SKIN_TRIANGLE)
        AnalyzerSkin.VOICE_RIBBON -> designSystem.icon(IconToken.SKIN_WAVE)
        AnalyzerSkin.EQUALIZER -> designSystem.icon(IconToken.SKIN_BARS)
        AnalyzerSkin.RINGS -> designSystem.icon(IconToken.SKIN_RINGS)
    }

    val skinName = when (currentSkin) {
        AnalyzerSkin.STATE_MAP -> designSystem.string(StringToken.SKIN_STATE_MAP)
        AnalyzerSkin.VOICE_RIBBON -> designSystem.string(StringToken.SKIN_VOICE_RIBBON)
        AnalyzerSkin.EQUALIZER -> designSystem.string(StringToken.SKIN_EQUALIZER)
        AnalyzerSkin.RINGS -> designSystem.string(StringToken.SKIN_RINGS)
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(designSystem.color(ColorToken.GLASS_BASE).copy(alpha = 0.2f))
            .clickable {
                val entries = AnalyzerSkin.entries
                val nextIndex = (currentSkin.ordinal + 1) % entries.size
                onSkinChange(entries[nextIndex])
            }
            .padding(if (showLabel) 6.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (showLabel) 32.dp else 40.dp)
                .background(designSystem.color(ColorToken.SURFACE_VARIANT), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = skinIcon,
                contentDescription = null,
                tint = designSystem.color(ColorToken.TEXT_PRIMARY),
                modifier = Modifier.size(if (showLabel) 18.dp else 24.dp)
            )
        }

        if (showLabel) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = skinName,
                color = designSystem.color(ColorToken.TEXT_PRIMARY).copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}
