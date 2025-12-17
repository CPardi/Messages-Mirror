package org.fossify.messages.receivers

import android.content.Context
import android.content.Intent
import android.os.Parcel
import org.cpardi.messagemirror.extensions.broadcastEvent
import org.cpardi.messagemirror.helpers.Constants
import org.cpardi.messagemirror.models.DeviceMode
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.EventMetadataDto

abstract class ForwardingSendStatusReceiver : SendStatusReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        val mode = DeviceMode.Companion.fromInt(prefs.getInt(Constants.MODE_NAME, -1))

        if (mode == DeviceMode.SmsHost) {
            val deviceID = prefs.getString(Constants.DEVICE_ID_NAME, "")
                .takeIf { !it.isNullOrEmpty() } ?: error("Device ID is null or empty")
            val parcel = Parcel.obtain()
            try {
                intent.writeToParcel(parcel, 0)
                val parcelBytes = parcel.marshall().toList()
                val metadata = EventMetadataDto(deviceID)
                val dto = EventDto.SmsSendStatus(metadata, parcelBytes)
                context.broadcastEvent(dto)

            } finally {
                parcel.recycle()
            }
        }

        super.onReceive(context, intent)
    }
}
