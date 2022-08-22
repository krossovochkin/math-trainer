import com.krossovochkin.common.App
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, state = WindowState(width = 400.dp, height = 800.dp)) {
        MaterialTheme {
            App()
        }
    }
}