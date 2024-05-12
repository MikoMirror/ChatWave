package screens.login

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screens.main.MainScreen
import screens.signUp.SignUpScreen
import serverServices.Constants
import serverServices.UserLogin

class LoginScreen() : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val userLogin = remember { UserLogin(Constants.SERVER_URL) }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                userLogin.loginUser(username, password) { success, token, message ->
                    if (success && token != null) {
                        // Navigate to MainScreen with username and token
                        navigator?.push(MainScreen(username, token))
                    } else {
                        // Handle login error
                        println(message)
                    }
                }
            }) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navigator?.push(SignUpScreen()) }) {
                Text("SingUp")
            }
        }
    }
}
