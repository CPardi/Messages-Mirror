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
import org.cpardi.messagemirror.models.LocalMsgId
import org.fossify.messages.receivers.SendStatusReceiver
import org.fossify.messages.receivers.MmsSentReceiver.Companion.EXTRA_ORIGINAL_RESENT_MESSAGE_ID

abstract class ForwardingSmsSendStatusReceiver : SendStatusReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val config = context.mirrorConfig
        if (config.mode != DeviceMode.SmsHost) return

        val originalId = intent.getLongExtra(EXTRA_ORIGINAL_RESENT_MESSAGE_ID, -1L)
        val originalLocalMsgId = if (originalId == -1L) null else LocalMsgId(originalId, false)
        val localMsgId = intent.data?.toLocalMsgId()!!

        val metadata = EventMetadataDto(config.deviceID)
        val dto = EventDto.SmsSendStatus(originalLocalMsgId ?: localMsgId, localMsgId, metadata, intent.serialise())
        context.messageMapStateMachine.processBlocking(dto)
    }
}
