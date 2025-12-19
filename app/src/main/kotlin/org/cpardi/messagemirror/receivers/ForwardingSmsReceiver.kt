package org.cpardi.messagemirror.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.cpardi.messagemirror.extensions.broadcastEvent
import org.cpardi.messagemirror.extensions.toByteArray
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.fossify.messages.receivers.SmsReceiver

class ForwardingSmsReceiver(private val wrappedReceiver: SmsReceiver = SmsReceiver()) :
    BroadcastReceiver() {
    companion object {
        const val SMS_DELIVER_ACTION = "android.provider.Telephony.SMS_DELIVER"
        const val NTFY_SEND_MESSAGE_ACTION = "io.heckel.ntfy.SEND_MESSAGE"
        const val NTFY_PACKAGE = "io.heckel.ntfy"
        const val NTFY_TOPIC = "topic"
        const val NTFY_MESSAGE = "message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SMS_DELIVER_ACTION) return

        val prefs = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        val deviceID = prefs.getString(Constants.DEVICE_ID_NAME, "").takeIf { !it.isNullOrEmpty() }
            ?: error("Device ID is null or empty")

        val intentBytes = intent.toByteArray().toList()
        val metadata = EventMetadataDto(deviceID)
        val dto: EventDto = EventDto.SmsReceive(metadata, intentBytes)
        context.broadcastEvent(dto)
        Log.d(Context::broadcastEvent.name, "Sent ntfy from device $deviceID")

        wrappedReceiver.onReceive(context, intent)
    }
}
