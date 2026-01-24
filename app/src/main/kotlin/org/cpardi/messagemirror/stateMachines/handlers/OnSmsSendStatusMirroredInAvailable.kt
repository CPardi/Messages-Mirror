package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toIntent
import org.cpardi.messagemirror.extensions.toUri
import org.cpardi.messagemirror.models.EventDto

private val TAG: String = OnSmsSendStatusMirroredInAvailable::class.qualifiedName!!

class OnSmsSendStatusMirroredInAvailable(val context: Context) {
    fun handle(state: Keyed<MessageMapState.Available>, dto: EventDto.SmsSendStatusMirrored): Keyed<MessageMapState>? {
        if (context.mirrorConfig.deviceID == dto.smsSendStatus.metadata.senderID) {
            Log.d(TAG, "Ignored ${dto::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        val intent = dto.smsSendStatus.intentParcel.toByteArray().toIntent()
        intent.data = state.item.localMsgId.toUri()
        context.sendBroadcast(intent)
        Log.d(TAG, "Broadcast intent of ${intent.action} with URI ${intent.data} from event received from ${dto.smsSendStatus.metadata.senderID}")
        return null
    }
}
