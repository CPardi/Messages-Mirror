package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toLong
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed
import org.fossify.messages.extensions.deleteMessageOnDevice
import org.fossify.messages.helpers.refreshConversations
import org.fossify.messages.helpers.refreshMessages

private val TAG = OnDeleteSmsMirroredInAvailable::class.qualifiedName!!

class OnDeleteSmsMirroredInAvailable(val context: Context) {
    fun handle(state: MessageMapState.Available, dto: EventDto.DeleteSmsMirrored): Keyed<MessageMapState>? {
        if (context.mirrorConfig.deviceID == dto.metadata.senderID) {
            Log.d(TAG, "Ignored ${dto::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        context.deleteMessageOnDevice(state.localMsgId.toLong(), dto.isMms)
        refreshMessages()
        refreshConversations()
        return Keyed(dto.globalMsgId, MessageMapState.Deleted(state.rowId))
    }
}
