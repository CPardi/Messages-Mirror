package org.cpardi.messagemirror.stateMachines.handlers

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.LocalMsgId
import org.fossify.messages.helpers.TXT_MIME_TYPE
import org.fossify.messages.messaging.sendMessageOnDeviceCompat
import org.fossify.messages.models.Attachment
import java.io.File
import java.io.FileOutputStream

private val TAG: String = OnSmsSendMirroredInMultipleStates::class.qualifiedName!!

class OnSmsSendMirroredInMultipleStates(val context: Context) {
    fun handle(keyedState: Keyed<MessageMapState>, sendMirrored: EventDto.SmsSendMirrored): Keyed<MessageMapState.Available>? {
        if (context.mirrorConfig.deviceID == sendMirrored.metadata.senderID) {
            Log.d(TAG, "Ignored ${sendMirrored::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        Log.d(TAG, "Begin processing SMS send as requested by device ${sendMirrored.metadata.senderID}")
        var localMsgId: LocalMsgId? = null
        val handleCreatedUri: (Uri) -> Unit = { uri ->
            localMsgId = uri.toLocalMsgId()
        }

        if (sendMirrored.globalMsgId.value.contains("mms")) {
            // We are unable to get an MMS' Id until the status is received. Therefore, create
            // a temp MMS to get an ID and update later
            val uri = context.contentResolver.insert(Telephony.Mms.Outbox.CONTENT_URI, ContentValues())!!
            localMsgId = uri.toLocalMsgId()
        }

        // Create avatars of attachments as text files.
        val attachments = mutableListOf<Attachment>()
        for (attachment in sendMirrored.attachments) {
            val file = File(context.filesDir, "${attachment.filename}.txt")
            val fos = FileOutputStream(file)
            fos.write("An attachment of type '${attachment.mimetype}' has been sent by the host.".toByteArray())
            fos.close()
            attachments.add(Attachment(id =null, messageId = -1, file.toURI().toString(), TXT_MIME_TYPE, width = 0, height = 0, attachment.filename))
        }

        context.sendMessageOnDeviceCompat(
            sendMirrored.text,
            sendMirrored.addresses,
            null, // Use the default subscription (SIM card) for the time being
            attachments,
            handleCreatedUri,
            localMsgId?.id,
        )

        Log.d(TAG, "Finish processing SMS send as requested by device ${sendMirrored.metadata.senderID}")

        val globalMsgId = keyedState.globalMsgId
        if(localMsgId == null) {
            Log.e(TAG, "Local message ID could not be obtained")
            return null
        }

        when (val state = keyedState.item) {
            is MessageMapState.Unknown -> {
                return Keyed(globalMsgId, MessageMapState.Available(globalMsgId, localMsgId))
            }

            is MessageMapState.Partial -> {
                val available = MessageMapState.Available(globalMsgId, localMsgId, state.rowId)
                OnSmsSendStatusInAvailable(context).handle(Keyed(globalMsgId, available), state.smsSendStatus)
                Log.d(TAG, "Processed status ${state.smsSendStatus.localMsgId} that was in Partial state")
                return Keyed(globalMsgId, available)
            }

            is MessageMapState.Available -> error("Event '${sendMirrored::class.simpleName}' not allowed when in state '${state::class.simpleName}'")
            is MessageMapState.Deleted -> error("Event '${sendMirrored::class.simpleName}' not allowed when in state '${state::class.simpleName}'")
        }
    }
}
