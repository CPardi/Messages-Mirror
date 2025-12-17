package org.cpardi.messagemirror.helpers

import android.content.Context
import android.provider.Telephony
import org.cpardi.messagemirror.models.EventDto
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getMyContactsCursor
import org.fossify.commons.helpers.SimpleContactsHelper
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.extensions.getThreadId
import org.fossify.messages.receivers.SmsReceiver

class SmsReceiveHandler(val deviceID: String) {
    fun handle(subscriptionId: Int, context: Context, dto: EventDto.SmsReceive) {
        if (deviceID == dto.metadata.senderID)
            return

        val address = dto.address
        val body = dto.body
        val subject = dto.subject
        val date = System.currentTimeMillis()
        val threadId = context.getThreadId(address)
        val status = Telephony.Sms.STATUS_NONE
        val type = Telephony.Sms.MESSAGE_TYPE_INBOX
        val read = 0

        val privateCursor = context.getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
        ensureBackgroundThread {
            if (context.baseConfig.blockUnknownNumbers) {
                val simpleContactsHelper = SimpleContactsHelper(context)
                simpleContactsHelper.exists(address, privateCursor) { exists ->
                    if (exists) {
                        SmsReceiver.Companion.handleMessage(
                            context,
                            address,
                            subject,
                            body,
                            date,
                            read,
                            threadId,
                            type,
                            subscriptionId,
                            status
                        )
                    }
                }
            } else {
                SmsReceiver.Companion.handleMessage(
                    context,
                    address,
                    subject,
                    body,
                    date,
                    read,
                    threadId,
                    type,
                    subscriptionId,
                    status
                )
            }
        }
    }
}
