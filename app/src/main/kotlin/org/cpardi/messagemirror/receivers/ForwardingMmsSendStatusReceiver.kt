package org.cpardi.messagemirror.receivers

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.cpardi.messagemirror.extensions.messageMapStateMachine
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.serialise
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.models.DeviceMode
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.cpardi.messagemirror.models.LocalMsgId
import org.fossify.messages.receivers.MmsSentReceiver
import org.fossify.messages.receivers.SendStatusReceiver

abstract class ForwardingMmsSendStatusReceiver : SendStatusReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val config = context.mirrorConfig

        val originalId = intent.getLongExtra(MmsSentReceiver.Companion.EXTRA_ORIGINAL_RESENT_MESSAGE_ID, -1L)
        val originalLocalMsgId = if (originalId == -1L) null else LocalMsgId(originalId, true)
        val updatedLocalMsgId = intent.getStringExtra(com.klinker.android.send_message.MmsSentReceiver.EXTRA_CONTENT_URI)?.toUri()?.toLocalMsgId()!!
        val localMsgId = originalLocalMsgId ?: updatedLocalMsgId

        val metadata = EventMetadataDto(config.deviceID)
        if (config.mode == DeviceMode.SmsHost) {
            val dto = EventDto.SmsSendStatus(localMsgId, updatedLocalMsgId, metadata, intent.serialise())
            context.messageMapStateMachine.processBlocking(dto)
        } else {
            val dto = EventDto.LocalMsgIdUpdated(localMsgId, updatedLocalMsgId)
            context.messageMapStateMachine.processBlocking(dto)
        }
    }
}
