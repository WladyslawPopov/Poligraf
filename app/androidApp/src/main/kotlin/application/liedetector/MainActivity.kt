package application.liedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import application.liedetector.navigation.NativeNavHost
import application.liedetector.navigation.navigationContext
import application.liedetector.presentation.main.MainComponent
import application.liedetector.presentation.root.RootComponent
import application.liedetector.theme.LieDetectorTheme
import application.liedetector.theme.ThemeState
import application.liedetector.ui.components.background.ScalesBackground
import application.liedetector.ui.screens.main.MainHost
import application.liedetector.uicore.theme.ColorToken
import application.liedetector.uicore.theme.DimenToken
import application.liedetector.uicore.theme.LocalDesignSystem
import application.liedetector.theme.utils.composeColor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            LieDetectorTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val designSystem = LocalDesignSystem.current

                val root = remember {
                    RootComponent(navigationContext()) {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                }

                NativeNavHost(
                    navigator = root.navigator,
                    drawerState = drawerState,
                    background = { ScalesBackground() },
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = designSystem.composeColor(ColorToken.SURFACE),
                            drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(
                                topEnd = designSystem.dimen(DimenToken.DRAWER_CORNER).dp, 
                                bottomEnd = designSystem.dimen(DimenToken.DRAWER_CORNER).dp
                            )
                        ) {
                            Spacer(modifier = Modifier.height(designSystem.dimen(DimenToken.SPACING_LARGE).dp))
                            Text(
                                text = "Settings",
                                modifier = Modifier.padding(designSystem.dimen(DimenToken.SPACING_MEDIUM).dp),
                                style = MaterialTheme.typography.headlineSmall,
                                color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = designSystem.dimen(DimenToken.SPACING_MEDIUM).dp)
                            )
                            
                            val isDark by ThemeState.isDark.collectAsState()
                            
                            ListItem(
                                headlineContent = { 
                                    Text("Dark Mode", color = designSystem.composeColor(ColorToken.TEXT_PRIMARY)) 
                                },
                                trailingContent = {
                                    Switch(
                                        checked = isDark,
                                        onCheckedChange = { ThemeState.toggle() }
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                            )
                        }
                    }
                ) { component ->
                    when (component) {
                        is MainComponent -> MainHost(component)
                        else -> Text("Loading...")
                    }
                }
            }
        }
    }
}
