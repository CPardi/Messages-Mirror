package org.cpardi.messagemirror.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import org.cpardi.messagemirror.extensions.broadcastEvent
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto
import org.cpardi.messagemirror.views.MirrorSettingsView
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.receivers.SmsReceiver

class ForwardingSmsReceiver(private val wrappedReceiver: SmsReceiver = SmsReceiver()) :
    BroadcastReceiver() {
    companion object {
        const val SMS_DELIVER_ACTION = "android.provider.Telephony.SMS_DELIVER"
        const val NTFY_SEND_MESSAGE_ACTION = "io.heckel.ntfy.SEND_MESSAGE"
        const val NTFY_PACKAGE = "io.heckel.ntfy"
        const val NTFY_TOPIC = "topic"
        const val NTFY_MESSAGE = "message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SMS_DELIVER_ACTION) return

        wrappedReceiver.onReceive(context, intent)
        val prefs = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(MirrorSettingsView.ENABLE_NAME, false)

        if (!isEnabled) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        var address = ""
        var body = ""
        var subject = ""
        var date = 0L
        var status = Telephony.Sms.STATUS_NONE

        ensureBackgroundThread {
            messages.forEach {
                address = it.originatingAddress ?: ""
                subject = it.pseudoSubject
                status = it.status
                body += it.messageBody
                date = System.currentTimeMillis()
            }

            val deviceID = prefs.getString(Constants.DEVICE_ID_NAME, "").takeIf { !it.isNullOrEmpty() }
                ?: error("Device ID is null or empty")
            val metadata = EventMetadataDto(deviceID)
            val dto: EventDto = EventDto.SmsReceive(metadata, address, subject, status, body, date)
            context.broadcastEvent(dto)
        }
    }
}
