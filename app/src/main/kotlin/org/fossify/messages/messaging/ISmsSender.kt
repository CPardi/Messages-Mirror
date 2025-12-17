package org.fossify.messages.messaging

import android.net.Uri
import org.cpardi.messagemirror.models.DeviceMode

interface ISmsSender {
    val forMode: DeviceMode

    fun sendMessage(subId: Int, destination: String, body: String, serviceCenter: String?, requireDeliveryReport: Boolean, messageUri: Uri)
}
