package org.cpardi.messagemirror.models

import android.content.Context
import org.fossify.messages.R

enum class DeviceMode(val value: Int) {
    None(0),
    SmsHost(1),
    Mirror(2);

    fun description(context: Context): String = when (this) {
        None -> context.getString(R.string.device_mode_none)
        SmsHost -> context.getString(R.string.device_mode_sms_host)
        Mirror -> context.getString(R.string.device_mode_mirror)
    }

    companion object {
        fun fromInt(value: Int): DeviceMode = entries.find { it.value == value }
            ?: throw IllegalArgumentException("Invalid Mode value: $value")
    }
}
