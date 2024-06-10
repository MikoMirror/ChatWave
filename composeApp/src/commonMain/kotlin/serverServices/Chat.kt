package serverServices

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateAsStringSerializer : KSerializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) =
        encoder.encodeString(dateFormat.format(value))

    override fun deserialize(decoder: Decoder): Date {
        return dateFormat.parse(decoder.decodeString()) ?: Date(0)
    }
}
@Serializable
data class Chat(
    @SerialName("_id") val id: String,
    val name: String,
    val participants: List<ChatParticipant>,
    val messages: List<ChatMessage>,
    val __v: Int? = null
)

@Serializable
data class ChatMessage(
    @SerialName("sender") val sender: String,
    val message: String,
    @Serializable(with = DateAsStringSerializer::class)
    val timestamp: Date = Date()
)


@Serializable
data class ChatParticipant(
    val _id: String,
    val username: String
)

@Serializable
data class ChatResponse(
    val chats: List<Chat>
)
@Serializable
data class FormattedChatMessage(val message: String)