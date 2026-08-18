package application.poligraf.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import application.poligraf.uicore.actions.WidgetAction
import application.poligraf.uicore.theme.DesignSystem
import application.poligraf.uicore.theme.LocalDesignSystem
import application.poligraf.uicore.theme.tokens.ColorToken
import application.poligraf.uicore.theme.tokens.DimenToken
import application.poligraf.uicore.theme.tokens.StringToken
import application.poligraf.uicore.widgets.UiWidget
import application.poligraf.widgets.utils.composeColor

@Composable
fun SubjectSliderRenderer(
    widget: UiWidget.SubjectSlider,
    onAction: (WidgetAction) -> Unit
) {
    val designSystem = LocalDesignSystem.current
    val state = rememberLazyListState()
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp)) {
        // Header for Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = designSystem.dimen(DimenToken.SPACING_LARGE).dp, 
                    vertical = designSystem.dimen(DimenToken.SPACING_SMALL).dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = designSystem.string(StringToken.SECTION_TEMPLATES).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = designSystem.composeColor(ColorToken.TEXT_SECONDARY),
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            state = state,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
            horizontalArrangement = Arrangement.spacedBy(widget.itemSpacing.dp)
        ) {
            items(widget.items, key = { it.id }) { item ->
                if (widget.displayMode == UiWidget.SubjectSlider.DisplayMode.RECT_STORY) {
                    SubjectStoryRenderer(item, designSystem, onAction)
                } else {
                    SubjectCardRenderer(item, designSystem, onAction)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectStoryRenderer(
    item: UiWidget.SubjectCard,
    designSystem: DesignSystem,
    onAction: (WidgetAction) -> Unit
) {
    Card(
        modifier = Modifier.width(designSystem.dimen(DimenToken.SUBJECT_STORY_WIDTH).dp)
            .height(designSystem.dimen(DimenToken.SUBJECT_STORY_HEIGHT).dp),
        colors = CardDefaults.cardColors(
            containerColor = designSystem.composeColor(item.backgroundColor).copy(alpha = 0.4f)
        ),
        shape = MaterialTheme.shapes.large,
        onClick = { onAction(item.action) },
        border = BorderStroke(
            designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp, 
            designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(designSystem.dimen(DimenToken.SPACING_SMALL).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = item.emoji, style = MaterialTheme.typography.headlineLarge)
            
            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp))
            
            Text(
                text = item.title ?: designSystem.string(item.titleToken),
                style = MaterialTheme.typography.labelSmall,
                color = designSystem.composeColor(item.titleColor),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                softWrap = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectCardRenderer(
    item: UiWidget.SubjectCard,
    designSystem: DesignSystem,
    onAction: (WidgetAction) -> Unit
) {
    Card(
        modifier = Modifier.width(designSystem.dimen(DimenToken.SUBJECT_CARD_WIDTH).dp)
            .height(designSystem.dimen(DimenToken.SUBJECT_CARD_HEIGHT).dp),
        colors = CardDefaults.cardColors(
            containerColor = designSystem.composeColor(item.backgroundColor).copy(alpha = 0.6f)
        ),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = { onAction(item.action) },
        border = BorderStroke(
            designSystem.dimen(DimenToken.DIVIDER_THICKNESS).dp, 
            designSystem.composeColor(ColorToken.GLASS_BORDER).copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(designSystem.dimen(DimenToken.SUBJECT_CARD_ICON_SIZE).dp)
                    .background(
                        designSystem.composeColor(item.buttonColor).copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, style = MaterialTheme.typography.displayMedium)
            }
            
            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp))
            
            Text(
                text = item.title ?: designSystem.string(item.titleToken),
                style = MaterialTheme.typography.titleMedium,
                color = designSystem.composeColor(item.titleColor),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                softWrap = true
            )
        }
    }
}
