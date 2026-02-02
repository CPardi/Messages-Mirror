package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed

private val TAG: String = OnSmsSendStatusMirroredInUnknown::class.qualifiedName!!

class OnSmsSendStatusMirroredInPartial(val context: Context) {
    fun handle(state: MessageMapState.Partial, dto: EventDto.SmsSendStatusMirrored): Keyed<MessageMapState>? {
        if (context.mirrorConfig.deviceID == dto.smsSendStatus.metadata.senderID) {
            Log.d(TAG, "Ignored ${dto::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

//        if(dto.smsSendStatus.action == SMS_SENT_ACTION) {
//            Log.d(TAG, "Ignored ${dto::class.simpleName} as SMS Sent only happens before delivered")
//            return null
//        }

        if(state.rowId == null) Log.d(TAG, "Existing ROWID is null")

        return Keyed(dto.globalMsgId, MessageMapState.Partial(dto.smsSendStatus, state.rowId))
    }
}
