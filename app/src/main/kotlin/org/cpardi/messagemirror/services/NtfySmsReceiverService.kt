package org.cpardi.messagemirror.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Telephony
import android.util.Base64
import androidx.core.content.ContextCompat
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver
import org.cpardi.messagemirror.activities.MirrorSettings
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getMyContactsCursor
import org.fossify.commons.extensions.showErrorToast
import org.fossify.commons.helpers.SimpleContactsHelper
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.extensions.getThreadId
import org.fossify.messages.receivers.SmsReceiver
import javax.crypto.spec.SecretKeySpec

class NtfySmsReceiverService : Service() {

    companion object {
        const val NTFY_RECEIVE_MESSAGE_ACTION = "io.heckel.ntfy.MESSAGE_RECEIVED"
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val prefs = context.getSharedPreferences(MirrorSettings.Companion.SETTINGS_NAME, MODE_PRIVATE)
            val isEnabled = prefs.getBoolean(MirrorSettings.Companion.ENABLE_NAME, false)
            val mode = MirrorSettings.DeviceMode.fromInt(prefs.getInt(MirrorSettings.Companion.MODE_NAME, MirrorSettings.DeviceMode.SmsHost.value))
            val subscribedTopic = prefs.getString(MirrorSettings.Companion.TOPIC_NAME, "")
            val topic = intent.getStringExtra(ForwardingSmsReceiver.Companion.NTFY_TOPIC)

            if (!isEnabled || mode != MirrorSettings.DeviceMode.Mirror || topic != subscribedTopic)
                return

            val keyBase64 = prefs.getString(MirrorSettings.Companion.ENCRYPTION_KEY_NAME, null)
            val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
            val key = SecretKeySpec(keyBytes, CryptoHelper.ALGORITHM)

            val encryptedMessage = intent.getStringExtra(ForwardingSmsReceiver.Companion.NTFY_MESSAGE) ?: return
            var decryptedMessage = ""
            try {
                decryptedMessage = CryptoHelper.decrypt(encryptedMessage, key)
            }
            catch (e: IllegalArgumentException) {
                context.showErrorToast(e)
            }

            val dto = EventDto.Companion.Serializer.decodeFromString<EventDto>(decryptedMessage)
            if (dto !is EventDto.SmsReceive) return

            val address = dto.address
            val body = dto.body
            val subject = dto.subject
            val date = System.currentTimeMillis()
            val threadId = context.getThreadId(address)
            val status = Telephony.Sms.STATUS_NONE
            val type = Telephony.Sms.MESSAGE_TYPE_INBOX
            val read = 0
            val subscriptionId = intent.getIntExtra("subscription", -1)

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

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(NTFY_RECEIVE_MESSAGE_ACTION)
        ContextCompat.registerReceiver(this, messageReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
