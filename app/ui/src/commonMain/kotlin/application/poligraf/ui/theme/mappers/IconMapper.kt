package application.poligraf.ui.theme.mappers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import application.poligraf.ui.theme.tokens.IconToken

internal object IconMapper {
    fun getIcon(token: IconToken): ImageVector {
        return when (token) {
            IconToken.MIC -> Icons.Default.Mic
            IconToken.HISTORY -> Icons.Default.History
            IconToken.SETTINGS -> Icons.Default.Settings
            IconToken.PROFILE -> Icons.Default.AccountCircle
            IconToken.CHEVRON_RIGHT -> Icons.Default.ChevronRight
            IconToken.MENU -> Icons.Default.Menu
            IconToken.CLOSE -> Icons.Default.Close
            IconToken.ARROW_BACK -> Icons.AutoMirrored.Filled.ArrowBack
            IconToken.GALLERY -> Icons.Default.Collections
            IconToken.NOTE -> Icons.AutoMirrored.Filled.NoteAdd
            IconToken.DELETE -> Icons.Default.Delete
            IconToken.DRAG_HANDLE -> Icons.Default.DragHandle
            IconToken.EDIT -> Icons.Default.Edit
            IconToken.CHECK -> Icons.Default.Check
            IconToken.MORE_VERT -> Icons.Default.MoreVert
            IconToken.MORE_HORIZ -> Icons.Default.MoreHoriz
            IconToken.PLAY -> Icons.Default.PlayArrow
            IconToken.PAUSE -> Icons.Default.Pause
            IconToken.SKIP_BACK_15 -> Icons.Default.Replay10
            IconToken.SKIP_FORWARD_15 -> Icons.Default.Forward10
            IconToken.SKIN_TRIANGLE -> Icons.Default.ChangeHistory
            IconToken.SKIN_WAVE -> Icons.Default.Waves
            IconToken.SKIN_BARS -> Icons.Default.BarChart
            IconToken.SKIN_RINGS -> Icons.Default.RadioButtonChecked
            IconToken.TRIM_HANDLE_LEFT -> Icons.Default.West
            IconToken.TRIM_HANDLE_RIGHT -> Icons.Default.East
        }
    }
}
