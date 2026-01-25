package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.LKeyed

private val TAG = OnDeleteSmsMirroredInUnknown::class.qualifiedName!!

class OnDeleteSmsMirroredInUnknown(val context: Context) {
    fun handle(dto: EventDto.DeleteSmsMirrored): LKeyed<MessageMapState.Unknown>? {
        if (context.mirrorConfig.deviceID == dto.metadata.senderID) {
            Log.d(TAG, "Ignored ${dto::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        Log.w(TAG, "SMS ${dto.globalMsgId} local deletion failed because mapping was unknown")
        return null
    }
}
