package org.cpardi.messagemirror.helpers

import android.content.ComponentName
import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.extensions.toIntent
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.receivers.RemoteSmsReceiver

private val TAG = SmsReceiveHandler::class.qualifiedName!!

class SmsReceiveHandler(val deviceID: String) {
    fun handle(context: Context, dto: EventDto.SmsReceive) {
        if (deviceID == dto.metadata.senderID) {
            Log.d(TAG, "Ignored event as sent from this device($deviceID)")
            return
        }

        val intent = dto.intentBytes.toByteArray().toIntent()
        intent.action = RemoteSmsReceiver::class.java.name
        intent.component = ComponentName(context, RemoteSmsReceiver::class.java)
        context.sendBroadcast(intent)
        Log.d(TAG, "Broadcast ${intent.action} Intent from event received from ${dto.metadata.senderID}")
    }
}
