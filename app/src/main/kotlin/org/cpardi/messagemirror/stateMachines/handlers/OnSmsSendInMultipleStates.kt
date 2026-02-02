package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.extensions.toGlobalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.toDto

class OnSmsSendInMultipleStates(val context: Context) {
    fun handle(send: EventDto.SmsSend): List<Keyed<MessageMapState.Available>> {
        val globalMsgIds = mutableListOf<GlobalMsgId>()
        val deviceId = context.mirrorConfig.deviceID
        val states = send.localMsgId.map { localMsgId ->
            val globalMsgId = localMsgId.toGlobalMsgId(deviceId)
            globalMsgIds.add(globalMsgId)
            Keyed(globalMsgId, MessageMapState.Available(globalMsgId, localMsgId))
        }

        val metadata = EventMetadataDto(deviceId)
        val attachments = send.attachments.map { it.toDto() }
        context.mirrorEvent(EventDto.SmsSendMirrored(globalMsgIds, metadata, send.text, send.addresses, send.subId, attachments, send.messageId))

        return states
    }
}
