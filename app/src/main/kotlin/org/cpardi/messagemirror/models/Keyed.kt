package org.cpardi.messagemirror.models

data class Keyed<out T>(val globalMsgId: GlobalMsgId, val item: T)
