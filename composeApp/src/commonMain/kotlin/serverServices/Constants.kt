package serverServices

import getPlatform

object Constants {
    val SERVER_URL: String
        get() {
            return if (getPlatform().isAndroid()) {
                "http://192.168.1.22:3000"
            } else {
                "http://localhost:3000"
            }
        }
}