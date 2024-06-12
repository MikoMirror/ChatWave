package screens.login

import ChatListScreen
import ChatListScreenParams
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screens.signUp.SignUpScreen
import serverServices.Constants
import serverServices.RestClient.UserLogin

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showErrorDialog by remember { mutableStateOf(false) }
        val userLogin = remember { UserLogin(Constants.SERVER_URL) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ChatWave", color = Color.White) },
                    backgroundColor = Color(0xFF17171F)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White), // Set a white background
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.Black) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.body1.copy(color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.Black) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = MaterialTheme.typography.body1.copy(color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        userLogin.loginUser(username, password) { success, token, message ->
                            if (success && token != null) {
                                navigator?.push(ChatListScreen(ChatListScreenParams(username, token)))
                            } else {
                                showErrorDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF17171F))
                ) {
                    Text("Login", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navigator?.push(SignUpScreen()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF17171F))
                ) {
                    Text("SingUp", color = Color.White)
                }

                if (showErrorDialog) {
                    AlertDialog(
                        onDismissRequest = { showErrorDialog = false },
                        title = { Text("Login Error") },
                        text = { Text("Invalid username or password.") },
                        confirmButton = {
                            Button(onClick = { showErrorDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}