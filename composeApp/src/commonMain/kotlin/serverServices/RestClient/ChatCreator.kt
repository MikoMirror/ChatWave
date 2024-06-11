package serverServices.RestClient

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import serverServices.Chat
import java.io.IOException

class ChatCreator(private val serverUrl: String, private val token: String) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun createChat(
        chatName: String,
        participantIds: List<String>,
        callback: (Boolean, Chat?, String?) -> Unit
    ) {
        val createChatRequest = CreateChatRequest(chatName, participantIds)
        val requestBody = json.encodeToString(createChatRequest)
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$serverUrl/api/chat/create")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null, "Error creating chat: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string()
                            val createChatResponse = responseBody?.let {
                                json.decodeFromString<CreateChatResponse>(it)
                            }
                            val newChat = createChatResponse?.chat?.copy(messages = emptyList())
                            callback(true, newChat, null)
                        } catch (e: Exception) {
                            callback(false, null, "Error parsing response: ${e.message}")
                        }
                    } else {
                        val errorBody = response.body?.string()
                        val errorMessage = "Error creating chat: ${response.code} - $errorBody"
                        callback(false, null, errorMessage)
                    }
                }
            }
        })
    }
}

@Serializable
data class CreateChatRequest(
    val name: String,
    val participantIds: List<String>
)

@Serializable
data class CreateChatResponse(
    val message: String,
    val chat: Chat
)