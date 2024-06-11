package serverServices.RestClient

import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import serverData.LoginResponse
import java.io.IOException


class UserLogin(private val serverUrl: String) {
    private val client = OkHttpClient()

    fun loginUser(username: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        val requestBody = "{\"username\":\"$username\", \"password\":\"$password\"}"
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$serverUrl/api/user/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false, null, "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string()
                            Logger.d { "Login Response: $responseBody" }
                            val jsonObject = Json.decodeFromString<LoginResponse>(responseBody ?: "")
                            callback(true, jsonObject.token, jsonObject.message)
                        } catch (e: Exception) {
                            Logger.e(e) { "Error parsing server response: ${e.message}" }
                            callback(false, null, "Error parsing server response: ${e.message}")
                        }
                    } else {
                        Logger.w { "Registration error: ${response.code}" }
                        callback(false, null, "Login error: ${response.code}")
                    }
                }
            }
        })
    }
}
