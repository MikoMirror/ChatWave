import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    var currentScreen by remember { mutableStateOf("Login") }

    Window(onCloseRequest = ::exitApplication, title = "ChatWave") {
        if (currentScreen == "Login") {
            LoginScreen {
                currentScreen = "AddNewUser"
            }
        } else if (currentScreen == "AddNewUser") {
            AddNewUser()
        }
    }
}