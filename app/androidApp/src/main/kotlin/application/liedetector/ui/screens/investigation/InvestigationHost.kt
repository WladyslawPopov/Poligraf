package application.liedetector.ui.screens.investigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import application.liedetector.presentation.investigation.InvestigationComponent
import application.liedetector.ui.components.AppScaffold
import application.liedetector.uicore.theme.*
import application.liedetector.theme.utils.composeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestigationHost(component: InvestigationComponent) {
    val state by component.viewModel.state.collectAsState()
    val designSystem = LocalDesignSystem.current

    AppScaffold(
        viewModel = component.viewModel,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(designSystem.string(StringToken.START_INVESTIGATION)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = designSystem.composeColor(ColorToken.BACKGROUND)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${designSystem.string(StringToken.INVESTIGATION_SCREEN_PLACEHOLDER)}: ${component.subjectId}",
                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
            )
        }
    }
}
