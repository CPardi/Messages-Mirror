package org.cpardi.messagemirror.messaging

import android.net.Uri
import android.util.Log
import org.cpardi.messagemirror.models.DeviceMode
import org.fossify.messages.messaging.ISmsSender

private val TAG: String = NullSmsSender::class.qualifiedName!!

class NullSmsSender : ISmsSender {
    override val forMode = DeviceMode.Mirror

    override fun sendMessage(subId: Int, destination: String, body: String, serviceCenter: String?, requireDeliveryReport: Boolean, messageUri: Uri) {
        Log.d(TAG, "Ignoring local send request for message $messageUri because device is $forMode")
    }
}
