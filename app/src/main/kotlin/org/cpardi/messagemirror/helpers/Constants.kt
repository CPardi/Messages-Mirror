package org.cpardi.messagemirror.helpers

object Constants {
    const val ACTION_SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER"
    const val ACTION_NTFY_RECEIVE_MESSAGE = "io.heckel.ntfy.MESSAGE_RECEIVED"
    const val ACTION_NTFY_SEND_MESSAGE = "io.heckel.ntfy.SEND_MESSAGE"

    const val PACKAGE_NTFY = "io.heckel.ntfy"

    const val INTENT_NTFY_MESSAGE = "message"
    const val INTENT_NTFY_BASE_URL = "base_url"
    const val INTENT_NTFY_TOPIC = "topic"

    const val SHARED_PREFERENCES_NAME = "org.cpardi.messagemirror"

    const val CONFIG_MODE = "mode"
    const val CONFIG_DEVICE_ID = "device_id"
    const val CONFIG_ENABLE = "mirror_enabled"
    const val CONFIG_TOPIC = "topic_name"
    const val CONFIG_ENCRYPTION_KEY = "encryption_key"
}
