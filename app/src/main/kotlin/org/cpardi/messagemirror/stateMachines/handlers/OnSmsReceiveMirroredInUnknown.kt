package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.messageMapStateMachine
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toIntent
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.LocalMsgId
import org.fossify.messages.receivers.SmsReceiver

private val TAG = OnSmsReceiveMirroredInUnknown::class.qualifiedName!!

class OnSmsReceiveMirroredInUnknown(val context: Context) {
    fun handle(dto: EventDto.SmsReceiveMirrored): List<Keyed<MessageMapState.Available>>? {
        if (context.mirrorConfig.deviceID == dto.metadata.senderID) {
            Log.d(TAG, "Ignored ${dto::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        val intent = dto.intentData.toIntent()
        val receiver = SmsReceiver { localId ->
            val localMsgId = LocalMsgId(localId, isMMS = false)
            val partDto = EventDto.SmsPartReceive(dto.globalMsgId, localMsgId, dto.metadata, intent)
            context.messageMapStateMachine.processBackground(partDto)
        }

        receiver.onReceive(context, intent)
        return null
    }
}
