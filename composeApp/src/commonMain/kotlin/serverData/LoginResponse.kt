package serverData

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val message: String
)