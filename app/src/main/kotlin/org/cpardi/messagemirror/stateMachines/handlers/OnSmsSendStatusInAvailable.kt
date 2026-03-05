package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.extensions.toGlobalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.GLKeyed
import org.cpardi.messagemirror.models.Keyed

class OnSmsSendStatusInAvailable(val context: Context) {
    fun handle(state: Keyed<MessageMapState.Available>, dto: EventDto.SmsSendStatus): GLKeyed<MessageMapState>? {
        if(dto.updatedLocalMsgId != null) {
            val updatedGlobalMsgId = dto.updatedLocalMsgId.toGlobalMsgId(context.mirrorConfig.deviceID)
            context.mirrorEvent(EventDto.SmsSendStatusMirrored(state.globalMsgId, updatedGlobalMsgId, dto))
            return GLKeyed(dto.updatedLocalMsgId, dto.updatedLocalMsgId.toGlobalMsgId(context.mirrorConfig.deviceID), state.item)
        }

        context.mirrorEvent(EventDto.SmsSendStatusMirrored(state.globalMsgId, null, dto))
        return null
    }
}
