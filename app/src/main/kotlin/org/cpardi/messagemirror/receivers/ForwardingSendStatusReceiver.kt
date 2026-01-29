package org.cpardi.messagemirror.receivers

import android.content.Context
import android.content.Intent
import org.cpardi.messagemirror.extensions.messageMapStateMachine
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.serialise
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.models.DeviceMode
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.fossify.messages.receivers.SendStatusReceiver

abstract class ForwardingSendStatusReceiver : SendStatusReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val config = context.mirrorConfig

        if (config.mode == DeviceMode.SmsHost) {
            val metadata = EventMetadataDto(config.deviceID)
            val localMsgId = intent.data?.toLocalMsgId()!!
            val dto = EventDto.SmsSendStatus(localMsgId, metadata, intent.serialise())
            context.messageMapStateMachine.process(dto)
        }

        super.onReceive(context, intent)
    }
}
