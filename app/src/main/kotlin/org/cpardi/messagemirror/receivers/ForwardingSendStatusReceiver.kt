package org.cpardi.messagemirror.receivers

import android.content.Context
import android.content.Intent
import org.cpardi.messagemirror.extensions.broadcastEvent
import org.cpardi.messagemirror.extensions.toByteArray
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.models.DeviceMode
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.fossify.messages.receivers.SendStatusReceiver

abstract class ForwardingSendStatusReceiver : SendStatusReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        val mode = DeviceMode.Companion.fromInt(prefs.getInt(Constants.MODE_NAME, -1))

        if (mode == DeviceMode.SmsHost) {
            val deviceID = prefs.getString(Constants.DEVICE_ID_NAME, "")
                .takeIf { !it.isNullOrEmpty() } ?: error("Device ID is null or empty")
            val intentByteArray = intent.toByteArray().toList()
            val metadata = EventMetadataDto(deviceID)
            val dto = EventDto.SmsSendStatus(metadata, intentByteArray)
            context.broadcastEvent(dto)
        }

        super.onReceive(context, intent)
    }
}
