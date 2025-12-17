package org.fossify.messages.messaging

import android.net.Uri
import org.cpardi.messagemirror.models.DeviceMode

class NullSmsSender : ISmsSender{
    override val forMode = DeviceMode.Mirror

    override fun sendMessage(subId: Int, destination: String, body: String, serviceCenter: String?, requireDeliveryReport: Boolean, messageUri: Uri) {
        // Intentionally empty
    }
}
