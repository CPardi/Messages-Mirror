package org.cpardi.messagemirror.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMap
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.extensions.messageMapDao
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.fromDto
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.helpers.refreshConversations
import org.fossify.messages.helpers.refreshMessages
import org.fossify.messages.messaging.sendMessageOnDeviceCompat

private val TAG: String = SmsSendHandler::class.qualifiedName!!

class SmsSendHandler(val deviceID: String) {
    fun handle(context: Context, dto: EventDto.SmsSend) {
        if (deviceID == dto.metadata.senderID) {
            Log.d(TAG, "Ignored event as sent from this device($deviceID)")
            return
        }

        ensureBackgroundThread {
            var i = 0
            val handleCreatedUri: (Uri) -> Unit = { uri ->
                val localMsgId = uri.toLocalMsgId()
                context.messageMapDao.insert(MessageMap(globalMsgId = dto.globalMsgIds[i], localMsgId = localMsgId))
                ++i
            }

            Log.d(TAG, "Begin sending SMS message locally as requested by device ${dto.metadata.senderID}")
            context.sendMessageOnDeviceCompat(
                dto.text,
                dto.addresses,
                dto.subId,
                dto.attachments.map { attachmentDto -> attachmentDto.fromDto() },
                handleCreatedUri,
                dto.messageId,
            )
            Log.d(TAG, "Finish sending SMS message locally as requested by device ${dto.metadata.senderID}")

            refreshMessages()
            refreshConversations()
        }
    }
}
