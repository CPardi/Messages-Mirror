package org.cpardi.messagemirror.extensions

import android.content.Context
import android.content.Intent
import android.util.Log
import org.cpardi.messagemirror.stateMachines.MessageMapStateMachine
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.helpers.CryptoHelper
import org.cpardi.messagemirror.helpers.MirrorConfig
import org.cpardi.messagemirror.models.EventDto

private const val TAG: String = "org.cpardi.messagemirror.extensions"

val Context.mirrorConfig: MirrorConfig
    get() = MirrorConfig(this)

val Context.messageMapStateMachine: MessageMapStateMachine
    get() = MessageMapStateMachine(this)

fun Context.mirrorEvent(dto: EventDto) {
    val config = this.mirrorConfig

    val message = EventDto.Serializer.encodeToString(dto)
    val encryptedMessage = CryptoHelper.encrypt(message, config.encryptionKey)

    val ntfyIntent = Intent(Constants.ACTION_NTFY_SEND_MESSAGE)
    ntfyIntent.setPackage(Constants.PACKAGE_NTFY)
    val baseUrl = config.baseUrl.toString()
    val topic = config.topic
    ntfyIntent.putExtra(Constants.INTENT_NTFY_BASE_URL, baseUrl)
    ntfyIntent.putExtra(Constants.INTENT_NTFY_TOPIC, topic)
    ntfyIntent.putExtra(Constants.INTENT_NTFY_MESSAGE, encryptedMessage)
    this.sendBroadcast(ntfyIntent)
    Log.d(TAG, "Broadcast intent of mirroring ${dto.javaClass.simpleName} event to baseURL '$baseUrl' with topic $topic")
}
