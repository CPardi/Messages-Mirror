package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.isEnabled
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.extensions.serialise
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed

class OnSmsPartReceiveInUnknown(val context: Context) {
    fun handle(dto: EventDto.SmsPartReceive): Keyed<MessageMapState> {
        if (context.mirrorConfig.isEnabled && context.mirrorConfig.deviceID == dto.metadata.senderID) {
            context.mirrorEvent(EventDto.SmsReceiveMirrored(dto.metadata, dto.globalMsgId, dto.intent.serialise()))
        }

        return Keyed(dto.globalMsgId, MessageMapState.Available(dto.globalMsgId, dto.localMsgId))
    }
}
