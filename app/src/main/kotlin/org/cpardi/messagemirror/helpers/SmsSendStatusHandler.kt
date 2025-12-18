package org.cpardi.messagemirror.helpers

import android.content.Context
import android.content.Intent
import android.os.Parcel
import org.cpardi.messagemirror.models.EventDto

class SmsSendStatusHandler(val deviceID: String) {
    fun handle(context: Context, dto: EventDto.SmsSendStatus) {
        if (deviceID == dto.metadata.senderID)
            return

        val parcel = Parcel.obtain()
        try {
            parcel.unmarshall(dto.intentParcel.toByteArray(), 0, dto.intentParcel.size)
            parcel.setDataPosition(0)
            val intent = Intent.CREATOR.createFromParcel(parcel)
            context.sendBroadcast(intent)
        } finally {
            parcel.recycle()
        }
    }
}
