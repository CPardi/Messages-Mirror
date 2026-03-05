package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.LKeyed
import org.fossify.messages.extensions.deleteMessageOnDevice

class OnDeleteSmsInAvailable(val context: Context) {
    fun handle(state: MessageMapState.Available, dto: EventDto.DeleteSms): LKeyed<MessageMapState.Deleted>? {
        context.deleteMessageOnDevice(dto.localMsgId.id, dto.localMsgId.isMMS)
        val mirroredDto = EventDto.DeleteSmsMirrored(state.globalMsgId, dto.metadata)
        context.mirrorEvent(mirroredDto)
        return LKeyed(dto.localMsgId, MessageMapState.Deleted(state.rowId))
    }
}
