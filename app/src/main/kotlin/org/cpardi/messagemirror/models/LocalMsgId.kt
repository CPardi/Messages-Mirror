package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class LocalMsgId(val value: String)
