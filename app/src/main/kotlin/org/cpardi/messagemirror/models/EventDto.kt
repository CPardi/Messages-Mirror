package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Polymorphic class representing the various events occurring on either the SMS host or Mirror. */
@Serializable
sealed class EventDto {
    /** Represents when an SMS message is received by the SMS host */
    @Serializable
    data class SmsReceive(
        val metadata: EventMetadataDto,
        val intentBytes: List<Byte>
    ) : EventDto()

    /** Represents when an SMS message is sent by a Mirror */
    @Serializable
    data class SmsSend(
        val metadata: EventMetadataDto,
        val text: String,
        val addresses: List<String>,
        val subId: Int?,
        val attachments: List<AttachmentDto>,
        val messageId: Long?
    ) : EventDto()

    /** Represents when an SMS message is sent by a Mirror */
    @Serializable
    data class SmsSendStatus(
        val metadata: EventMetadataDto,
        val intentParcel: List<Byte>
    ) : EventDto()

    companion object {
        /** Enables polymorphic serialisation for this class */
        val Serializer: Json
            get() = Json {
                serializersModule = SerializersModule {
                    polymorphic(EventDto::class) {
                        subclass(SmsReceive::class)
                        subclass(SmsSend::class)
                        subclass(SmsSendStatus::class)
                    }
                }
                classDiscriminator = "type"
                ignoreUnknownKeys = true
            }
    }
}
