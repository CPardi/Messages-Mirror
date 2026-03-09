package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.LKeyed
import org.fossify.messages.extensions.deleteMessageOnDevice

private val TAG = OnDeleteSmsInUnknown::class.qualifiedName!!

class OnDeleteSmsInUnknown(val context: Context) {
    fun handle(dto: EventDto.DeleteSms): LKeyed<MessageMapState.Unknown>? {
        context.deleteMessageOnDevice(dto.localMsgId.id, dto.localMsgId.isMMS)
        Log.w(TAG, "SMS ${dto.localMsgId} deletion mirror failed because mapping is unknown")
        return null
    }
}
