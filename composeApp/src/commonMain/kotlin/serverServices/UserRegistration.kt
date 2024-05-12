package serverServices

import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class UserRegistration(private val serverUrl: String) {
    private val client = OkHttpClient()

    fun registerUser(username: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            "{\"username\":\"$username\", \"email\":\"$email\", \"password\":\"$password\"}"
        )

        val request = Request.Builder()
            .url("$serverUrl/api/user/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false, "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            try {
                                val jsonObject = JSONObject(responseBody)
                                val message = jsonObject.getString("message")
                                callback(true, message)
                            } catch (e: JSONException) {
                                callback(false, "Error parsing server response")
                            }
                        } else {
                            callback(false, "No response body")
                        }
                    } else {
                        callback(false, "Registration error: ${response.code}")
                    }
                }
            }
        })
    }
}