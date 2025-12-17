package org.cpardi.messagemirror.models

enum class DeviceMode(val value: Int) {
    SmsHost(1),
    Mirror(2);

    fun description(): String = when (this) {
        SmsHost -> "SMS Host"
        Mirror -> "Mirror"
    }

    companion object {
        fun fromInt(value: Int): DeviceMode = entries.find { it.value == value }
            ?: throw IllegalArgumentException("Invalid Mode value: $value")
    }
}
