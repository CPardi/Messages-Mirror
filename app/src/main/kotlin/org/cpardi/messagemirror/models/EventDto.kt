package org.cpardi.messagemirror.models

import android.content.Intent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.fossify.messages.models.Attachment

/** Polymorphic class representing the various events occurring on either the SMS host or Mirror. */
@Serializable
sealed class EventDto {
    /** Represents when an SMS message is received by a host device */
    data class SmsReceive(
        val metadata: EventMetadataDto,
        val intent: Intent
    ) : EventDto()

    /** Represents when an SMS has been assigned a local msg id by the host device */
    data class SmsPartReceive(
        val globalMsgId: GlobalMsgId,
        val localMsgId: LocalMsgId,
        val metadata: EventMetadataDto,
        val intent: Intent
    ) : EventDto()

    /** Represents when an SMS that has been received is ready to be mirrored */
    @Serializable
    data class SmsReceiveMirrored(
        val metadata: EventMetadataDto,
        val globalMsgId: GlobalMsgId,
        val intentData: String
    ) : EventDto()

    /** Represents when an SMS message is sent from a host device */
    data class SmsSend(
        val localMsgId: LocalMsgId,
        val text: String,
        val addresses: List<String>,
        val subId: Int?,
        val attachments: List<Attachment>,
        val messageId: Long?
    ) : EventDto()

    /** Represents when an SMS that has been sent is ready to be mirrored */
    @Serializable
    data class SmsSendMirrored(
        val globalMsgId: GlobalMsgId,
        val metadata: EventMetadataDto,
        val text: String,
        val addresses: List<String>,
        val subId: Int?,
        val attachments: List<AttachmentDto>,
        val messageId: Long?
    ) : EventDto()

    /** Represents when an SMS message status is updated on a host device */
    @Serializable
    data class SmsSendStatus(
        val localMsgId: LocalMsgId,
        val updatedLocalMsgId: LocalMsgId?,
        val metadata: EventMetadataDto,
        val intentData: String,
    ) : EventDto()

    /** Represents when an local message ID is updated (this is done for MMS) */
    data class LocalMsgIdUpdated(
        val localMsgId: LocalMsgId,
        val updatedLocalMsgId: LocalMsgId
    ) : EventDto()

    /** Represents when an SMS status is ready to be mirrored */
    @Serializable
    data class SmsSendStatusMirrored(
        val globalMsgId: GlobalMsgId,
        val updatedGlobalMsgId: GlobalMsgId?,
        val smsSendStatus: SmsSendStatus
    ) : EventDto()

    /** Represents when an SMS message has been deleted on a host device */
    data class DeleteSms(
        val localMsgId: LocalMsgId,
        val metadata: EventMetadataDto
    ) : EventDto()

    /** Represents when an SMS deletion is ready to be mirrored */
    @Serializable
    data class DeleteSmsMirrored(
        val globalMsgId: GlobalMsgId,
        val metadata: EventMetadataDto,
    ) : EventDto()


    companion object {
        /** Enables polymorphic serialisation for this class */
        val Serializer: Json
            get() = Json {
                serializersModule = SerializersModule {
                    polymorphic(EventDto::class) {
                        subclass(SmsReceiveMirrored::class)
                        subclass(SmsSendMirrored::class)
                        subclass(SmsSendStatusMirrored::class)
                        subclass(DeleteSmsMirrored::class)
                    }
                }
                explicitNulls = false
                prettyPrint = false
                classDiscriminator = "type"
                ignoreUnknownKeys = true
            }
    }
}
