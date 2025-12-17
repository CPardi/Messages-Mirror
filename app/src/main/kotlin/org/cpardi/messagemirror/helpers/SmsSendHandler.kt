package org.cpardi.messagemirror.helpers

import android.content.Context
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.fromDto
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.helpers.refreshConversations
import org.fossify.messages.helpers.refreshMessages
import org.fossify.messages.messaging.sendMessageOnDeviceCompat

class SmsSendHandler(val deviceID: String) {
    fun handle(context: Context, dto: EventDto.SmsSend) {
        if (deviceID == dto.metadata.senderID)
            return

        ensureBackgroundThread {
            context.sendMessageOnDeviceCompat(
                dto.text,
                dto.addresses,
                dto.subId,
                dto.attachments.map { attachmentDto -> attachmentDto.fromDto() },
                dto.messageId
            )

            refreshMessages()
            refreshConversations()
        }
    }
}
