import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import screens.main.ChatRoomScreen
import screens.main.ChatRoomScreenParams
import serverServices.Chat
import serverServices.ChatFetcher
import serverServices.Constants.SERVER_URL
import serverServices.JwtTokenDecoder
import serverServices.RestClient.ChatCreator
import serverServices.User
import serverServices.UserFetcher

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
        var showDialog by remember { mutableStateOf(false) }
        val chatCreator = remember { ChatCreator(SERVER_URL, token) }
        val coroutineScope = rememberCoroutineScope()

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
                    onClick = { showDialog = true },
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
        if (showDialog) {
            CreateChatPopup(
                token = token,
                userId = userId,
                chatCreator = chatCreator,
                onChatCreated = { newChat ->
                    showDialog = false
                    if (newChat != null) {
                        chats = chats + newChat
                        navigator.push(ChatRoomScreen(ChatRoomScreenParams(newChat.id, token, username)))
                    }
                }
            ) {
                showDialog = false
            }
        }


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
    }
}

@Composable
fun CreateChatPopup(
    token: String,
    userId: String,
    chatCreator: ChatCreator,
    onChatCreated: (Chat?) -> Unit,
    onDismiss: () -> Unit
) {
    var chatName by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    val searchedUsers = remember { mutableStateListOf<User>() }
    val coroutineScope = rememberCoroutineScope()
    val userFetcher = remember { UserFetcher(SERVER_URL, token) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = chatName,
                    onValueChange = { chatName = it },
                    label = { Text("Chat Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                var searchText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        coroutineScope.launch {
                            if (searchText.length > 2) {
                                val result = userFetcher.searchUserByUsername(searchText) { searchResult ->
                                    if (searchResult.isSuccess) {
                                        searchedUsers.clear()
                                        searchedUsers.addAll(searchResult.getOrDefault(emptyList()))
                                    } else {
                                        println("Error searching for users: ${searchResult.exceptionOrNull()?.message}")
                                    }
                                }
                            } else {
                                searchedUsers.clear()
                            }
                        }
                    },
                    label = { Text("Search for User") },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn {
                    items(searchedUsers) { user ->
                        Text(
                            text = user.username,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUser = user
                                    searchText = user.username
                                }
                                .padding(8.dp)
                                .background(
                                    if (selectedUser == user) Color.LightGray else Color.Transparent
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (chatName.isNotBlank() && selectedUser != null) {
                            coroutineScope.launch { // Now you can use coroutineScope here
                                chatCreator.createChat(chatName, listOf(userId, selectedUser!!._id)) { success, newChat, errorMessage ->
                                    if (success) {
                                        onChatCreated(newChat)
                                        chatName = ""
                                        selectedUser = null
                                    } else {
                                        println("Error creating chat: $errorMessage")
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Chat")
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