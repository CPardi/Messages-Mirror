package org.cpardi.messagemirror.helpers

import android.content.Context
import android.content.Intent
import android.os.Parcel
import org.cpardi.messagemirror.extensions.toIntent
import org.cpardi.messagemirror.models.EventDto

class SmsSendStatusHandler(val deviceID: String) {
    fun handle(context: Context, dto: EventDto.SmsSendStatus) {
        if (deviceID == dto.metadata.senderID)
            return

        val intent = dto.intentParcel.toByteArray().toIntent()
        context.sendBroadcast(intent)
    }
}
