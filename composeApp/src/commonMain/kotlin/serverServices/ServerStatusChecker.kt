package serverServices

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class ServerStatusChecker(private val serverUrl: String) {
    private val client = OkHttpClient()

    fun checkServerStatus(callback: (Boolean, String?) -> Unit) {
        val request = Request.Builder()
            .url("$serverUrl/")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false, "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        callback(true, "Server is up and running")
                    } else {
                        callback(false, "Server error: ${response.code}")
                    }
                }
            }
        })
    }
}