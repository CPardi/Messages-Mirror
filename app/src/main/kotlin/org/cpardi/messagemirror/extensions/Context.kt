package org.cpardi.messagemirror.extensions

import android.content.Context
import android.content.Intent
import android.util.Base64
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.helpers.MirrorConfig
import org.cpardi.messagemirror.models.EventDto
import org.fossify.commons.helpers.ensureBackgroundThread
import javax.crypto.spec.SecretKeySpec

val Context.mirrorConfig: MirrorConfig
    get() = MirrorConfig(this)

fun Context.broadcastEvent(dto: EventDto) {
    val config = this.mirrorConfig
    val keyBytes = Base64.decode(config.encryptionKey, Base64.NO_WRAP)
    val key = SecretKeySpec(keyBytes, CryptoHelper.ALGORITHM)

    ensureBackgroundThread {
        val message = EventDto.Serializer.encodeToString(dto)
        val encryptedMessage = CryptoHelper.encrypt(message, key)

        val ntfyIntent = Intent(Constants.NTFY_SEND_MESSAGE_ACTION)
        ntfyIntent.setPackage(Constants.NTFY_PACKAGE)
        ntfyIntent.putExtra(Constants.NTFY_TOPIC, config.topic)
        ntfyIntent.putExtra(Constants.NTFY_MESSAGE, encryptedMessage)
        this.sendBroadcast(ntfyIntent)
    }
}
