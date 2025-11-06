package org.cpardi.messagemirror

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.provider.Telephony
import android.util.Base64
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.receivers.SmsReceiver
import javax.crypto.spec.SecretKeySpec

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

        val prefs = context.getSharedPreferences(MirrorSettings.SETTINGS_NAME, MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(MirrorSettings.ENABLE_NAME, false)
        val mode = MirrorSettings.DeviceMode.fromInt(prefs.getInt(MirrorSettings.MODE_NAME, MirrorSettings.DeviceMode.SmsHost.value))
        val topic = prefs.getString(MirrorSettings.TOPIC_NAME, "")

        if (!isEnabled || mode != MirrorSettings.DeviceMode.SmsHost) return

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

            val dto : EventDto = EventDto.SmsReceive(address, subject, status, body, date)
            val message = EventDto.Serializer.encodeToString(dto)

            val keyBase64 = prefs.getString(MirrorSettings.ENCRYPTION_KEY_NAME, null)
            val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
            val key = SecretKeySpec(keyBytes, CryptoHelper.ALGORITHM)

            val encryptedMessage = CryptoHelper.encrypt(message, key)

            val ntfyIntent = Intent(NTFY_SEND_MESSAGE_ACTION)
            ntfyIntent.setPackage(NTFY_PACKAGE)
            ntfyIntent.putExtra(NTFY_TOPIC, topic)
            ntfyIntent.putExtra(NTFY_MESSAGE, encryptedMessage)
            context.sendBroadcast(ntfyIntent)
        }
    }
}
