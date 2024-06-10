package serverServices

import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException

class ChatFetcher(private val serverUrl: String, private val token: String) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true } // Ignore unknown keys

    fun fetchChats(userId: String, callback: (Result<List<Chat>>) -> Unit) {
        val request = Request.Builder()
            .url("$serverUrl/api/chat/user/$userId")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string()
                            val chatResponse = json.decodeFromString<ChatResponse>(responseBody ?: "")
                            callback(Result.success(chatResponse.chats))
                        } catch (e: Exception) {
                            callback(Result.failure(Exception("Error parsing chats response: ${e.message}")))
                        }
                    } else {
                        callback(Result.failure(Exception("Error fetching chats: ${response.code}")))
                    }
                }
            }
        })
    }
}