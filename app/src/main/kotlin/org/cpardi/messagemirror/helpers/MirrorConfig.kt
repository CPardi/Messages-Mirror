package org.cpardi.messagemirror.helpers

import android.content.Context
import org.cpardi.messagemirror.helpers.Constants.DEVICE_ID_NAME
import org.cpardi.messagemirror.helpers.Constants.ENABLE_NAME
import org.cpardi.messagemirror.helpers.Constants.ENCRYPTION_KEY_NAME
import org.cpardi.messagemirror.helpers.Constants.MODE_NAME
import org.cpardi.messagemirror.helpers.Constants.SETTINGS_NAME
import org.cpardi.messagemirror.helpers.Constants.TOPIC_NAME
import org.cpardi.messagemirror.models.DeviceMode
import java.util.UUID

class MirrorConfig(context: Context) {
    private val prefs = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    private val editor = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE).edit()

    val deviceID: String
        get() {
            if (!prefs.contains(DEVICE_ID_NAME)) {
                val uniqueID = UUID.randomUUID().toString()
                editor.putString(DEVICE_ID_NAME, uniqueID).apply()
            }

            return prefs.getString(DEVICE_ID_NAME, UUID.randomUUID().toString())!!
        }

    var enabled: Boolean
        get() = prefs.getBoolean(ENABLE_NAME, false)
        set(enabled) = editor.putBoolean(ENABLE_NAME, enabled).apply()

    var topic: String
        get() = prefs.getString(TOPIC_NAME, "")!!
        set(enabled) = editor.putString(TOPIC_NAME, enabled).apply()

    var encryptionKey: String
        get() = prefs.getString(ENCRYPTION_KEY_NAME, "")!!
        set(enabled) = editor.putString(ENCRYPTION_KEY_NAME, enabled).apply()

    var mode: DeviceMode
        get() = DeviceMode.fromInt(prefs.getInt(MODE_NAME, 1))
        set(enabled) = editor.putInt(MODE_NAME, enabled.value).apply()
}
