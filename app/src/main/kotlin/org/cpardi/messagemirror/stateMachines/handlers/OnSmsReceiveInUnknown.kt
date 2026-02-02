package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.messageMapStateMachine
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toGlobalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.LocalMsgId
import org.fossify.messages.receivers.SmsReceiver

class OnSmsReceiveInUnknown(val context: Context) {
    fun handle(dto: EventDto.SmsReceive): List<Keyed<MessageMapState>>? {
        val receiver = SmsReceiver { localId ->
            val localMsgId = LocalMsgId(localId.toString())
            val partDto = EventDto.SmsPartReceive(localMsgId.toGlobalMsgId(context.mirrorConfig.deviceID), localMsgId, dto.metadata, dto.intent)
            context.messageMapStateMachine.processBackground(partDto)
        }

        receiver.onReceive(context, dto.intent)
        return null
    }
}
