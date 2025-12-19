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
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.helpers.SmsReceiveHandler
import org.cpardi.messagemirror.helpers.SmsSendHandler
import org.cpardi.messagemirror.helpers.SmsSendStatusHandler
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver
import org.cpardi.messagemirror.views.MirrorSettingsView
import org.fossify.commons.extensions.showErrorToast
import org.fossify.messages.R
import org.fossify.messages.activities.MainActivity
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.SecretKeySpec

class EventConsumerService : Service() {

    companion object {
        const val NTFY_RECEIVE_MESSAGE_ACTION = "io.heckel.ntfy.MESSAGE_RECEIVED"
    }

    class BootStartReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ServiceManager(context).refresh()
        }
    }

    class AutoRestartReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ServiceManager(context).refresh()
        }
    }

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val prefs = context.getSharedPreferences(Constants.SETTINGS_NAME, MODE_PRIVATE)
            val isEnabled = prefs.getBoolean(MirrorSettingsView.Companion.ENABLE_NAME, false)
            val subscribedTopic = prefs.getString(MirrorSettingsView.Companion.TOPIC_NAME, "")
            val deviceID = prefs.getString(Constants.DEVICE_ID_NAME, "").takeIf { !it.isNullOrEmpty() }
                ?: error("Device ID is null or empty")
            val topic = intent.getStringExtra(ForwardingSmsReceiver.Companion.NTFY_TOPIC)

            if (!isEnabled || topic != subscribedTopic)
                return

            val keyBase64 = prefs.getString(MirrorSettingsView.Companion.ENCRYPTION_KEY_NAME, null)
            val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
            val key = SecretKeySpec(keyBytes, CryptoHelper.ALGORITHM)

            val encryptedMessage = intent.getStringExtra(ForwardingSmsReceiver.Companion.NTFY_MESSAGE) ?: return
            var decryptedMessage = ""
            try {
                decryptedMessage = CryptoHelper.decrypt(encryptedMessage, key)
            } catch (e: Exception) {
                when (e) {
                    is IndexOutOfBoundsException,
                    is IllegalArgumentException,
                    is IllegalBlockSizeException,
                    is BadPaddingException -> {
                        context.showErrorToast(e)
                        return
                    }

                    else -> throw e
                }
            }

            val dto = EventDto.Companion.Serializer.decodeFromString<EventDto>(decryptedMessage)
            when (dto) {
                is EventDto.SmsReceive -> SmsReceiveHandler(deviceID).handle(context, dto)
                is EventDto.SmsSend -> SmsSendHandler(deviceID).handle(context, dto)
                is EventDto.SmsSendStatus -> SmsSendStatusHandler(deviceID).handle(context, dto)
                else -> return
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(NTFY_RECEIVE_MESSAGE_ACTION)
        ContextCompat.registerReceiver(this, eventReceiver, filter, ContextCompat.RECEIVER_EXPORTED)

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


        val channelName = "Messages Mirror active"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW).let {
            it.setShowBadge(false)
            it
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val id = 1
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, id, notification, foregroundServiceType)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY;
    }

    override fun onDestroy() {
        unregisterReceiver(eventReceiver)
        sendBroadcast(Intent(this, AutoRestartReceiver::class.java)) // Restart if necessary
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
