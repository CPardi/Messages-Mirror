package org.cpardi.messagemirror.helpers

import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.extensions.toUri
import org.cpardi.messagemirror.extensions.messageMapDao
import org.cpardi.messagemirror.extensions.toIntent
import org.cpardi.messagemirror.models.EventDto
import org.fossify.commons.helpers.ensureBackgroundThread

private val TAG: String = SmsSendStatusHandler::class.qualifiedName!!

class SmsSendStatusHandler(val deviceID: String) {

    fun handle(context: Context, dto: EventDto.SmsSendStatus) {
        if (deviceID == dto.metadata.senderID) {
            Log.d(TAG, "Ignored event as sent from this device($deviceID)")
            return
        }

        ensureBackgroundThread {
            val intent = dto.intentParcel.toByteArray().toIntent()
            val dao = context.messageMapDao
            val map = dao.getByGlobalMsgId(dto.globalMsgId)
            intent.data = map.localMsgId.toUri()

            context.sendBroadcast(intent)
            Log.d(TAG, "Broadcast ${intent.action} Intent with URI ${intent.data} from event received from ${dto.metadata.senderID}")
        }
    }
}
