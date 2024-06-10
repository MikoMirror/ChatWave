package screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import serverServices.ChatMessage
import serverServices.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class ChatRoomScreenParams(val chatId: String, val token: String, val username: String)

class ChatRoomScreen(private val params: ChatRoomScreenParams) : Screen {
    @Composable
    override fun Content() {
        val chatId = params.chatId
        val token = params.token
        val username = params.username

        var newMessageText by remember { mutableStateOf("") }
        var messages by remember { mutableStateOf<List<String>>(emptyList()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val socket = remember { IO.socket(Constants.SERVER_URL) }
        val json = Json { ignoreUnknownKeys = true }

        LaunchedEffect(Unit) {
            try {
                socket.connect()
                socket.emit("authenticate", token)
                socket.emit("join-chat", chatId)
                socket.on("chat-history") { args ->
                    runCatching {
                        messages = json.decodeFromString<List<String>>(args[0].toString())
                    }.onFailure { e ->
                        errorMessage = "Error parsing chat history: ${e.message}"
                    }
                }
                socket.on("chat-message") { args ->
                    runCatching {
                        val newMessage = json.decodeFromString<ChatMessage>(args[0].toString())
                        val formattedMessage = "[${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}]<${newMessage.sender}> ${newMessage.message}"
                        messages = messages + formattedMessage
                    }.onFailure { e ->
                        errorMessage = "Error processing new message: ${e.message}"
                    }
                }
                socket.on(Socket.EVENT_CONNECT_ERROR) {
                    errorMessage = "Connection Error"
                }
            } catch (e: Exception) {
                errorMessage = "Error connecting to chat: ${e.message}"
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                socket.disconnect()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chat Room", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    backgroundColor = Color(0xFF17171F),
                    contentColor = Color.White
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                errorMessage?.let {
                    Text(
                        text = "Error: $it",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        MessageCard(message = message, currentUser = username)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = newMessageText,
                        onValueChange = { newMessageText = it },
                        placeholder = { Text("Type your message...") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (newMessageText.isNotBlank()) {
                            val message = ChatMessage(sender = username, message = newMessageText)
                            val messageJson = json.encodeToString(message)
                            socket.emit("chat-message", messageJson)
                            newMessageText = ""
                        }
                    }) {
                        Icon(Icons.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageCard(message: String, currentUser: String) {
    val parts = message.split("<")
    val sender = parts.getOrNull(1)?.split(">")?.getOrNull(0)?.trim() ?: ""
    val isCurrentUser = sender == currentUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .background(if (isCurrentUser) Color.Blue else Color.LightGray),
        ) {
            Text(
                text = message,
                fontWeight = FontWeight.Normal,
                color = if (isCurrentUser) Color.White else Color.Black
            )
        }
    }
}