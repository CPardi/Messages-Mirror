package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageAttachmentDto(
    val id: Long,
    val text: String,
    val attachments: ArrayList<AttachmentDto>
)
