package application.poligraf.presentation.debug.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.poligraf.presentation.debug.DebugViewModel

@Composable
fun StatesTab(viewModel: DebugViewModel, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding
    ) {
        item {
            Text(
                "Current Component State",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        // ... list of states
    }
}
