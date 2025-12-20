package org.cpardi.messagemirror.receivers

import android.content.Context
import android.content.Intent
import org.cpardi.messagemirror.extensions.broadcastEvent
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toByteArray
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.models.DeviceMode
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.fossify.messages.receivers.SendStatusReceiver

abstract class ForwardingSendStatusReceiver : SendStatusReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val config = context.mirrorConfig

        if (config.mode == DeviceMode.SmsHost) {
            val intentByteArray = intent.toByteArray().toList()
            val metadata = EventMetadataDto(config.deviceID)
            val dto = EventDto.SmsSendStatus(metadata, intentByteArray)
            context.broadcastEvent(dto)
        }

        super.onReceive(context, intent)
    }
}
