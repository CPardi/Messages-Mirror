package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.util.Log
import com.klinker.android.send_message.MmsSentReceiver.EXTRA_CONTENT_URI
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toIntent
import org.cpardi.messagemirror.extensions.toUri
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.GLKeyed
import org.cpardi.messagemirror.models.Keyed
import org.fossify.messages.receivers.MmsSentReceiver.Companion.EXTRA_ORIGINAL_RESENT_MESSAGE_ID

private val TAG: String = OnSmsSendStatusMirroredInAvailable::class.qualifiedName!!

class OnSmsSendStatusMirroredInAvailable(val context: Context) {
    fun handle(state: Keyed<MessageMapState.Available>, dto: EventDto.SmsSendStatusMirrored): GLKeyed<MessageMapState>? {
        if (context.mirrorConfig.deviceID == dto.smsSendStatus.metadata.senderID) {
            Log.d(TAG, "Ignored ${dto::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        val intent = dto.smsSendStatus.intentData.toIntent()
        if (state.item.localMsgId.isMMS) {
            intent.putExtra(EXTRA_ORIGINAL_RESENT_MESSAGE_ID, null as Long?)
            intent.putExtra(EXTRA_CONTENT_URI, state.item.localMsgId.toUri().toString())
        } else {
            intent.data = state.item.localMsgId.toUri()
        }

        context.sendBroadcast(intent)
        Log.d(TAG, "Broadcast intent of ${intent.action} with URI ${intent.data} from event received from ${dto.smsSendStatus.metadata.senderID}")

        return if (dto.updatedGlobalMsgId != null) GLKeyed(state.item.localMsgId, dto.updatedGlobalMsgId, state.item) else null
    }
}
