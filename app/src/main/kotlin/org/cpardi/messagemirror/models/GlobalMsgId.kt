package org.cpardi.messagemirror.models

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class GlobalMsgId(val value: String)
