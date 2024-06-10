import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import screens.main.ChatRoomScreen
import screens.main.ChatRoomScreenParams
import serverServices.Chat
import serverServices.ChatFetcher
import serverServices.Constants
import serverServices.Constants.SERVER_URL
import serverServices.JwtTokenDecoder

data class ChatListScreenParams(val username: String, val token: String)

class ChatListScreen(private val params: ChatListScreenParams) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val jwtDecoder = JwtTokenDecoder()
        val username = params.username
        val token = params.token
        val userId = jwtDecoder.getUserIdFromToken(token) ?: ""
        var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val chatFetcher = remember { ChatFetcher(SERVER_URL, token) }

        LaunchedEffect(Unit) {
            chatFetcher.fetchChats(userId) { result ->
                if (result.isSuccess) {
                    chats = result.getOrDefault(emptyList())
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "An error occurred"
                    println("Error fetching chats: $errorMessage")
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Messages", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    backgroundColor = Color(0xFF17171F),
                    contentColor = Color.White,
                    actions = {
                        IconButton(onClick = { /* TODO: Navigate to Search */ }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO: Navigate to Create New Chat */ },
                    backgroundColor = Color(0xFF17171F)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (errorMessage != null) {
                    item {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(chats) { chat ->
                        ChatListItem(chat) { chatId ->
                            navigator.push(ChatRoomScreen(ChatRoomScreenParams(chatId, token, username)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: Chat, onChatClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChatClick(chat.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "${chat.name} (${chat.participants.firstOrNull()?.username ?: ""})",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = chat.messages.lastOrNull()?.message ?: "")
        }
    }
}