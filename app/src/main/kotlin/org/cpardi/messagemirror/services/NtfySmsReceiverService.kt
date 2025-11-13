package org.cpardi.messagemirror.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver
import org.cpardi.messagemirror.views.MirrorSettingsView
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getMyContactsCursor
import org.fossify.commons.extensions.showErrorToast
import org.fossify.commons.helpers.SimpleContactsHelper
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.R
import org.fossify.messages.activities.MainActivity
import org.fossify.messages.extensions.getThreadId
import org.fossify.messages.receivers.SmsReceiver
import javax.crypto.spec.SecretKeySpec


class NtfySmsReceiverService : Service() {

    companion object {
        const val NTFY_RECEIVE_MESSAGE_ACTION = "io.heckel.ntfy.MESSAGE_RECEIVED"
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val prefs = context.getSharedPreferences(MirrorSettingsView.Companion.SETTINGS_NAME, MODE_PRIVATE)
            val isEnabled = prefs.getBoolean(MirrorSettingsView.Companion.ENABLE_NAME, false)
            val mode = MirrorSettingsView.DeviceMode.fromInt(prefs.getInt(MirrorSettingsView.Companion.MODE_NAME, MirrorSettingsView.DeviceMode.SmsHost.value))
            val subscribedTopic = prefs.getString(MirrorSettingsView.Companion.TOPIC_NAME, "")
            val topic = intent.getStringExtra(ForwardingSmsReceiver.Companion.NTFY_TOPIC)

            if (!isEnabled || mode != MirrorSettingsView.DeviceMode.Mirror || topic != subscribedTopic)
                return

            val keyBase64 = prefs.getString(MirrorSettingsView.Companion.ENCRYPTION_KEY_NAME, null)
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

        val channelId = "messagesMirror-subscriber"
        val notificationGroupId = "org.cpardi.messagemirror.NOTIFICATION_GROUP"
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Listening for messages")
            .setSmallIcon(R.drawable.ic_mirror_vector)
            .setContentIntent(pendingIntent) // Open Messages Mirror on tap
            .setSound(null)
            .setShowWhen(false) // Don't show time
            .setOngoing(true)
            .setGroup(notificationGroupId)
            .build()



        val channelName = "MessageMirrorChannel"
        val channel = NotificationChannel(channelId,  channelName, NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val id = 1
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, id, notification, foregroundServiceType)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
