package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.extensions.toGlobalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.toDto

class OnSmsSendInMultipleStates(val context: Context) {
    fun handle(send: EventDto.SmsSend): Keyed<MessageMapState.Available> {
        val deviceId = context.mirrorConfig.deviceID

        val globalMsgId = send.localMsgId.toGlobalMsgId(deviceId)
        val metadata = EventMetadataDto(deviceId)
        val attachments = send.attachments.map { it.toDto() }
        context.mirrorEvent(EventDto.SmsSendMirrored(globalMsgId, metadata, send.text, send.addresses, send.subId, attachments, send.messageId))

        return Keyed(globalMsgId, MessageMapState.Available(globalMsgId, send.localMsgId))
    }
}
