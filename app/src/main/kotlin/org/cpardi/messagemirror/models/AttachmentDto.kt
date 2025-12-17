package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable
import org.fossify.messages.models.Attachment

@Serializable
data class AttachmentDto(
    val id: Long?,
    val messageId: Long,
    val uriString: String,
    val mimetype: String,
    val width: Int,
    val height: Int,
    val filename: String
)

fun AttachmentDto.fromDto(): Attachment {
    return Attachment(
        this.id,
        this.messageId,
        this.uriString,
        this.mimetype,
        this.width,
        this.height,
        this.filename
    )
}

fun Attachment.toDto(): AttachmentDto {
    return AttachmentDto(
        this.id,
        this.messageId,
        this.uriString,
        this.mimetype,
        this.width,
        this.height,
        this.filename
    )
}
