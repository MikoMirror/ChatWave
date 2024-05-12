import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.Navigator
import screens.login.LoginScreen
import serverServices.Constants
import serverServices.ServerStatusChecker

@Composable
fun App() {
    val serverStatusChecker = remember { ServerStatusChecker(Constants.SERVER_URL) }
    var isServerUp by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        serverStatusChecker.checkServerStatus { isUp, message ->
            isServerUp = isUp
            statusMessage = message ?: ""
        }
    }

    if (!isServerUp) {
        Text("Server is down: $statusMessage")
    } else {
        Navigator(LoginScreen())
    }
}