package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed

private val TAG: String = OnSmsSendStatusMirroredInUnknown::class.qualifiedName!!

class OnSmsSendStatusMirroredInUnknown(val context: Context) {
    fun handle(dto: EventDto.SmsSendStatusMirrored): Keyed<MessageMapState>? {
        if (context.mirrorConfig.deviceID == dto.smsSendStatus.metadata.senderID) {
            Log.d(TAG, "Ignored ${dto::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        return Keyed(dto.globalMsgId, MessageMapState.Partial(dto.smsSendStatus))
    }
}
