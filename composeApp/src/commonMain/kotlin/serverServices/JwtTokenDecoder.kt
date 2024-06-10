package serverServices

import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.JwtConsumerBuilder

class JwtTokenDecoder {

    fun getUserIdFromToken(token: String): String? {
        try {
            val jwtClaims: JwtClaims = JwtConsumerBuilder()
                .setSkipSignatureVerification()
                .build()
                .processToClaims(token)
            return jwtClaims.getStringClaimValue("userId")
        } catch (e: Exception) {
            println("Error decoding JWT: ${e.message}")
            return null
        }
    }
}