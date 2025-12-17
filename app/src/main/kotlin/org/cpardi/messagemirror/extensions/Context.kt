package org.cpardi.messagemirror.extensions

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Base64
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver.Companion.NTFY_MESSAGE
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver.Companion.NTFY_PACKAGE
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver.Companion.NTFY_SEND_MESSAGE_ACTION
import org.cpardi.messagemirror.receivers.ForwardingSmsReceiver.Companion.NTFY_TOPIC
import org.cpardi.messagemirror.views.MirrorSettingsView
import org.fossify.commons.helpers.ensureBackgroundThread
import javax.crypto.spec.SecretKeySpec

fun Context.broadcastEvent(prefs: SharedPreferences, dto: EventDto) {
    val topic = prefs.getString(MirrorSettingsView.Companion.TOPIC_NAME, "")
    val keyBase64 = prefs.getString(MirrorSettingsView.ENCRYPTION_KEY_NAME, null)
    val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
    val key = SecretKeySpec(keyBytes, CryptoHelper.ALGORITHM)

    ensureBackgroundThread {
        val message = EventDto.Serializer.encodeToString(dto)
        val encryptedMessage = CryptoHelper.encrypt(message, key)

        val ntfyIntent = Intent(NTFY_SEND_MESSAGE_ACTION)
        ntfyIntent.setPackage(NTFY_PACKAGE)
        ntfyIntent.putExtra(NTFY_TOPIC, topic)
        ntfyIntent.putExtra(NTFY_MESSAGE, encryptedMessage)
        this.sendBroadcast(ntfyIntent)
    }
}
