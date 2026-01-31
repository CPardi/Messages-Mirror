package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.isEnabled
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.extensions.toLong
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.LKeyed
import org.fossify.messages.extensions.deleteMessageOnDevice

class OnDeleteSmsInAvailable(val context: Context) {
    fun handle(state: MessageMapState.Available, dto: EventDto.DeleteSms): LKeyed<MessageMapState.Deleted>? {
        context.deleteMessageOnDevice(dto.localMsgId.toLong(), dto.isMms)

        if(context.mirrorConfig.isEnabled) {
            val mirroredDto = EventDto.DeleteSmsMirrored(state.globalMsgId, dto.metadata, dto.isMms)
            context.mirrorEvent(mirroredDto)
        }

        return LKeyed(dto.localMsgId, MessageMapState.Deleted(state.rowId))
    }
}
