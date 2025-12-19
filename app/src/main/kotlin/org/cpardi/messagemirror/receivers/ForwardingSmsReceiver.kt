package org.cpardi.messagemirror.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.cpardi.messagemirror.extensions.broadcastEvent
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toByteArray
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.fossify.messages.receivers.SmsReceiver

class ForwardingSmsReceiver(private val wrappedReceiver: SmsReceiver = SmsReceiver()) :
    BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Constants.SMS_DELIVER_ACTION) return

        val config = context.mirrorConfig
        val intentBytes = intent.toByteArray().toList()
        val metadata = EventMetadataDto(config.deviceID)
        val dto: EventDto = EventDto.SmsReceive(metadata, intentBytes)
        context.broadcastEvent(dto)
        Log.d(Context::broadcastEvent.name, "Sent ntfy from device ${config.deviceID}")

        wrappedReceiver.onReceive(context, intent)
    }
}
