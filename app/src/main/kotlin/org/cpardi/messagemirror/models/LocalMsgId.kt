package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable

@Serializable
data class LocalMsgId(val id: Long, val isMMS: Boolean)
