package org.cpardi.messagemirror.helpers

import android.content.Context
import org.cpardi.messagemirror.helpers.Constants.CONFIG_BASE_URL
import org.cpardi.messagemirror.helpers.Constants.CONFIG_DEVICE_ID
import org.cpardi.messagemirror.helpers.Constants.CONFIG_ENCRYPTION_KEY
import org.cpardi.messagemirror.helpers.Constants.CONFIG_MODE
import org.cpardi.messagemirror.helpers.Constants.CONFIG_TOPIC
import org.cpardi.messagemirror.helpers.Constants.SHARED_PREFERENCES_NAME
import org.cpardi.messagemirror.helpers.Constants.URL_NTFY_DEFAULT
import org.cpardi.messagemirror.models.DeviceMode
import java.net.URI
import java.util.UUID

class MirrorConfig(context: Context) {
    private val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()

    val deviceID: String
        get() {
            if (!prefs.contains(CONFIG_DEVICE_ID)) {
                val uniqueID = UUID.randomUUID().toString()
                editor.putString(CONFIG_DEVICE_ID, uniqueID).apply()
            }

            return prefs.getString(CONFIG_DEVICE_ID, UUID.randomUUID().toString())!!
        }

    var mode: DeviceMode
        get() = DeviceMode.fromInt(prefs.getInt(CONFIG_MODE, 0))
        set(it) = editor.putInt(CONFIG_MODE, it.value).apply()

    var topic: String
        get() = prefs.getString(CONFIG_TOPIC, "")!!
        set(it) = editor.putString(CONFIG_TOPIC, it).apply()

    var baseUrl: URI?
        get() {
            val baseUrl = prefs.getString(CONFIG_BASE_URL, "")
            return if (baseUrl.isNullOrBlank()) URI(URL_NTFY_DEFAULT) else URI(baseUrl)
        }
        set(it) = editor.putString(CONFIG_BASE_URL, it.toString()).apply()

    var encryptionKey: String
        get() = prefs.getString(CONFIG_ENCRYPTION_KEY, "")!!
        set(it) = editor.putString(CONFIG_ENCRYPTION_KEY, it).apply()
}
