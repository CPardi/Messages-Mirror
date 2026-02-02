package org.cpardi.messagemirror.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.cpardi.messagemirror.extensions.messageMapStateMachine
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto

class ForwardingSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Constants.ACTION_SMS_DELIVER) return

        val config = context.mirrorConfig
        val metadata = EventMetadataDto(config.deviceID)
        val dto = EventDto.SmsReceive(metadata, intent)

        context.messageMapStateMachine.processBlocking(dto)
    }
}
