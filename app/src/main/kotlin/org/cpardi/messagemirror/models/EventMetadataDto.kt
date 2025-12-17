package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable

@Serializable
data class EventMetadataDto(val senderID: String)
