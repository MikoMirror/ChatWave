package serverServices

import getPlatform

object Constants {
    val SERVER_URL: String
        get() {
            return if (getPlatform().isAndroid()) {
                "http://10.0.2.2:3000"
            } else {
                "http://localhost:3000"
            }
        }
}