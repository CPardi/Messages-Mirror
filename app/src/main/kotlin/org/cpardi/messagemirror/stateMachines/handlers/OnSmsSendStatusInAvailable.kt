package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed

class OnSmsSendStatusInAvailable(val context: Context) {
    fun handle(state: Keyed<MessageMapState.Available>, dto: EventDto.SmsSendStatus): Keyed<MessageMapState>? {
        context.mirrorEvent(EventDto.SmsSendStatusMirrored(state.globalMsgId, dto))
        return null
    }
}
