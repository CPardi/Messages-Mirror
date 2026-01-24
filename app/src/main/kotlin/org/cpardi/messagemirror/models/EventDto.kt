package org.cpardi.messagemirror.models

import android.content.Intent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Polymorphic class representing the various events occurring on either the SMS host or Mirror. */
@Serializable
sealed class EventDto {
    /** Represents when an SMS message is received */
    data class SmsReceive(
        val metadata: EventMetadataDto,
        val intent: Intent
    ) : EventDto()

    /** Represents when an SMS message is received */
    data class SmsPartReceive(
        val globalMsgId: GlobalMsgId,
        val localMsgId: LocalMsgId,
        val metadata: EventMetadataDto,
        val intent: Intent
    ) : EventDto()

    @Serializable
    data class SmsReceiveMirrored(
        val metadata: EventMetadataDto,
        val globalMsgId: GlobalMsgId,
        val intentBytes: List<Byte>
    ) : EventDto()

    /** Represents when an SMS message is sent */
    @Serializable
    data class SmsSend(
        val metadata: EventMetadataDto,
        val text: String,
        val addresses: List<String>,
        val subId: Int?,
        val attachments: List<AttachmentDto>,
        val messageId: Long?
    ) : EventDto()

    @Serializable
    data class SmsSendMirrored(
        val globalMsgIds: List<GlobalMsgId>,
        val smsSend: SmsSend
    ) : EventDto()

    /** Represents when an SMS message status is updated */
    @Serializable
    data class SmsSendStatus(
        val localMsgId: LocalMsgId,
        val metadata: EventMetadataDto,
        val intentParcel: List<Byte>
    ) : EventDto()

    @Serializable
    data class SmsSendStatusMirrored(
        val globalMsgId: GlobalMsgId,
        val smsSendStatus: SmsSendStatus
    ) : EventDto()


    companion object {
        /** Enables polymorphic serialisation for this class */
        val Serializer: Json
            get() = Json {
                serializersModule = SerializersModule {
                    polymorphic(EventDto::class) {
                        subclass(SmsReceiveMirrored::class)
                        subclass(SmsSend::class)
                        subclass(SmsSendMirrored::class)
                        subclass(SmsSendStatus::class)
                        subclass(SmsSendStatusMirrored::class)
                    }
                }
                classDiscriminator = "type"
                ignoreUnknownKeys = true
            }
    }
}
