package org.cpardi.messagemirror.models

data class LKeyed<out T>(val localMsgId: LocalMsgId, val item: T)
