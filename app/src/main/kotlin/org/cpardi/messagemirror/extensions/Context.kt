package org.cpardi.messagemirror.extensions

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import org.cpardi.messagemirror.databases.LoggingMessageMapDao
import org.cpardi.messagemirror.databases.MessageMapDao
import org.cpardi.messagemirror.databases.MirrorDatabase
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.helpers.MirrorConfig
import org.cpardi.messagemirror.models.EventDto
import org.fossify.commons.helpers.ensureBackgroundThread
import javax.crypto.spec.SecretKeySpec

private const val TAG: String = "org.cpardi.messagemirror.extensions"

val Context.mirrorConfig: MirrorConfig
    get() = MirrorConfig(this)

private fun Context.getDb() = MirrorDatabase.Holder.getInstance(this)

val Context.messageMapDao: MessageMapDao
    get() = LoggingMessageMapDao(baseDao = getDb().MessageMapDao())

fun Context.mirrorEvent(dto: EventDto) {
    val config = this.mirrorConfig
    val keyBytes = Base64.decode(config.encryptionKey, Base64.NO_WRAP)
    val key = SecretKeySpec(keyBytes, CryptoHelper.ALGORITHM)

    ensureBackgroundThread {
        Log.d(TAG, "Mirroring ${dto.javaClass.simpleName} event")

        val message = EventDto.Serializer.encodeToString(dto)
        val encryptedMessage = CryptoHelper.encrypt(message, key)

        val ntfyIntent = Intent(Constants.ACTION_NTFY_SEND_MESSAGE)
        ntfyIntent.setPackage(Constants.PACKAGE_NTFY)
        ntfyIntent.putExtra(Constants.INTENT_NTFY_TOPIC, config.topic)
        ntfyIntent.putExtra(Constants.INTENT_NTFY_MESSAGE, encryptedMessage)
        this.sendBroadcast(ntfyIntent)
    }
}
