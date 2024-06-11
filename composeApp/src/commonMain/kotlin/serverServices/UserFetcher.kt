package serverServices

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
class UserFetcher(private val serverUrl: String, private val token: String) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun searchUserByUsername(username: String, callback: (Result<List<User>>) -> Unit) {
        val requestBody = FindByUsernameRequest(username)
            .let { json.encodeToString(it) }
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$serverUrl/api/user/findByUsername")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(Exception("Error searching user: ${e.message}")))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string()
                            val userResponse = responseBody?.let {
                                json.decodeFromString<FindByUsernameResponse>(it)
                            }
                            val users = if (userResponse?.user != null) listOf(userResponse.user) else emptyList()
                            callback(Result.success(users))
                        } catch (e: Exception) {
                            callback(Result.failure(Exception("Error parsing response: ${e.message}")))
                        }
                    } else {
                        val errorBody = response.body?.string()
                        val errorMessage = "Error searching user: ${response.code} - $errorBody"
                        callback(Result.failure(Exception(errorMessage)))
                    }
                }
            }
        })
    }
}

@Serializable
data class FindByUsernameRequest(
    val username: String
)

@Serializable
data class FindByUsernameResponse(
    val user: User?
)


