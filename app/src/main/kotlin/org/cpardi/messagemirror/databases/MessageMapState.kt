package org.cpardi.messagemirror.databases

import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

sealed class MessageMapState {
    data class Unknown(val dummy: Unit = Unit) : MessageMapState()
    data class Partial(val smsSendStatus: EventDto.SmsSendStatus, val rowId: Long? = null) : MessageMapState()
    data class Available(val globalMsgId: GlobalMsgId, val localMsgId: LocalMsgId, val rowId: Long? = null) : MessageMapState()
}
