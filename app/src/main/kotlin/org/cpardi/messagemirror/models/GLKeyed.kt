package org.cpardi.messagemirror.models

data class GLKeyed<out T>(val localMsgId: LocalMsgId, val globalMsgId: GlobalMsgId, val item: T)
