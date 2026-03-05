package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.LKeyed

class OnSmsSendStatusInUnknown(val context: Context) {
    fun handle(dto: EventDto.SmsSendStatus): LKeyed<MessageMapState> {
        return LKeyed(dto.updatedLocalMsgId ?: dto.localMsgId, MessageMapState.Partial(dto))
    }
}
