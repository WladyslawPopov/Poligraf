package application.poligraf

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import application.poligraf.engine.component.asAppComponentContext
import application.poligraf.engine.device.AndroidPermissionManager
import application.poligraf.engine.device.AppPermission
import application.poligraf.engine.device.PermissionManager
import application.poligraf.presentation.App
import application.poligraf.presentation.root.DefaultRootComponent
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.asJetpackComponentContext
import com.arkivanov.decompose.retainedComponent
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val permissionManager: PermissionManager by inject()

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            (permissionManager as? AndroidPermissionManager)?.notifyPermissionResult(
                AppPermission.RECORD_AUDIO,
                isGranted
            )
        }

        (permissionManager as? AndroidPermissionManager)?.setRequestAction { permission ->
            when (permission) {
                AppPermission.RECORD_AUDIO -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(
                componentContext = componentContext.asJetpackComponentContext()
                    .asAppComponentContext()
            )
        }
        
        setContent {
            App(
                root = root,
            )
        }
    }
}
