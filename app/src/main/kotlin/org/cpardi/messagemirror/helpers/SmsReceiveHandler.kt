package org.cpardi.messagemirror.helpers

import android.content.ComponentName
import android.content.Context
import android.util.Log
import org.cpardi.messagemirror.extensions.broadcastEvent
import org.cpardi.messagemirror.extensions.toIntent
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.receivers.RemoteSmsReceiver

class SmsReceiveHandler(val deviceID: String) {

    fun handle(context: Context, dto: EventDto.SmsReceive) {
        if (deviceID == dto.metadata.senderID) {
            Log.d(Context::broadcastEvent.name, "Ignored remote SMS Receive message on device $deviceID")
            return
        }

        val intent = dto.intentBytes.toByteArray().toIntent()
        intent.action = RemoteSmsReceiver::class.java.name
        intent.component = ComponentName(context, RemoteSmsReceiver::class.java)
        context.sendBroadcast(intent)
        Log.d(Context::broadcastEvent.name, "Broadcast remote SMS Receive message on device $deviceID")
    }
}
