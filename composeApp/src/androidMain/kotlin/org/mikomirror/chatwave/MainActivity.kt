package org.mikomirror.chatwave
import AddNewUser
import LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember { mutableStateOf("Login") }

            if (currentScreen == "Login") {
                LoginScreen {
                    currentScreen = "AddNewUser"
                }
            } else if (currentScreen == "AddNewUser") {
                AddNewUser()
            }
        }
    }
}
