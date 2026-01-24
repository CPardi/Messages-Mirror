package org.cpardi.messagemirror.messaging

import android.net.Uri
import android.util.Log
import org.fossify.messages.messaging.ISmsSender

private val TAG: String = LoggingSmsSender::class.qualifiedName!!

class LoggingSmsSender(val sender: ISmsSender) : ISmsSender {
    override val forMode = sender.forMode

    override fun sendMessage(subId: Int, destination: String, body: String, serviceCenter: String?, requireDeliveryReport: Boolean, messageUri: Uri) {
        sender.sendMessage(subId, destination, body, serviceCenter, requireDeliveryReport, messageUri)
        Log.d(TAG, "Sent SMS message $messageUri using local SIM because device is $forMode")
    }
}
