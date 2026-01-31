package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.net.Uri
import android.util.Log
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.isEnabled
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.extensions.toGlobalMsgId
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.fromDto
import org.fossify.messages.messaging.sendMessageOnDeviceCompat

private val TAG: String = OnSmsSendInMultipleStates::class.qualifiedName!!

class OnSmsSendInMultipleStates(val context: Context) {
    fun handle(send: EventDto.SmsSend): List<Keyed<MessageMapState.Available>> {
        Log.d(TAG, "Begin processing SMS send as requested by device ${send.metadata.senderID}")
        val states = mutableListOf<Keyed<MessageMapState.Available>>()
        val handleCreatedUri: (Uri) -> Unit = { uri ->
            val localMsgId = uri.toLocalMsgId()
            val globalMsgId = localMsgId.toGlobalMsgId(context.mirrorConfig.deviceID)
            val available = MessageMapState.Available(globalMsgId, localMsgId)
            states.add(Keyed(globalMsgId, available))
        }

        context.sendMessageOnDeviceCompat(
            send.text,
            send.addresses,
            send.subId,
            send.attachments.map { attachmentDto -> attachmentDto.fromDto() },
            handleCreatedUri,
            send.messageId,
        )

        Log.d(TAG, "Finish processing SMS send as requested by device ${send.metadata.senderID}")

        if(context.mirrorConfig.isEnabled) {
            context.mirrorEvent(EventDto.SmsSendMirrored(states.map { it.globalMsgId }, send))
        }

        return states.toList()
    }
}
