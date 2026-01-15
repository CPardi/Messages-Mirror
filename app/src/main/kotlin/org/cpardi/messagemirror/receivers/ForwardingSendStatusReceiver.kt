package org.cpardi.messagemirror.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.extensions.messageMapDao
import org.cpardi.messagemirror.extensions.mirrorEvent
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toByteArray
import org.cpardi.messagemirror.models.DeviceMode
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.receivers.SendStatusReceiver

private val TAG: String = ForwardingSendStatusReceiver::class.qualifiedName!!

abstract class ForwardingSendStatusReceiver : SendStatusReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val config = context.mirrorConfig

        if (config.mode == DeviceMode.SmsHost) {
            ensureBackgroundThread {
                val messageMapDao = context.messageMapDao
                val map = messageMapDao.getByLocalMsgId(intent.data?.toLocalMsgId()!!)
                val intentByteArray = intent.toByteArray().toList()
                val metadata = EventMetadataDto(config.deviceID)
                val dto = EventDto.SmsSendStatus(metadata, map.globalMsgId, intentByteArray)
                context.mirrorEvent(dto)
                Log.d(TAG, "Raised ${EventDto.SmsSendStatus::class.simpleName} event for message on ${config.deviceID}")
            }
        }

        super.onReceive(context, intent)
    }
}
