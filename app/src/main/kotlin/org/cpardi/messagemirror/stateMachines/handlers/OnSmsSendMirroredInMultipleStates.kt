package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import android.net.Uri
import android.util.Log
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.LocalMsgId
import org.cpardi.messagemirror.models.fromDto
import org.fossify.messages.messaging.sendMessageOnDeviceCompat

private val TAG: String = OnSmsSendMirroredInMultipleStates::class.qualifiedName!!

class OnSmsSendMirroredInMultipleStates(val context: Context) {
    fun handle(keyedStates: List<Keyed<MessageMapState>>, sendMirrored: EventDto.SmsSendMirrored): List<Keyed<MessageMapState.Available>>? {
        if (context.mirrorConfig.deviceID == sendMirrored.metadata.senderID) {
            Log.d(TAG, "Ignored ${sendMirrored::class.simpleName} as sent from this device(${context.mirrorConfig.deviceID})")
            return null
        }

        Log.d(TAG, "Begin processing SMS send as requested by device ${sendMirrored.metadata.senderID}")
        val localMsgIds = mutableListOf<LocalMsgId>()
        val handleCreatedUri: (Uri) -> Unit = { uri ->
            localMsgIds.add(uri.toLocalMsgId())
        }

        context.sendMessageOnDeviceCompat(
            sendMirrored.text,
            sendMirrored.addresses,
            null, // Use the default subscription (SIM card) for the time being
            sendMirrored.attachments.map { attachmentDto -> attachmentDto.fromDto() },
            handleCreatedUri,
            sendMirrored.messageId,
        )

        Log.d(TAG, "Finish processing SMS send as requested by device ${sendMirrored.metadata.senderID}")

        val globalToLocalMap = sendMirrored.globalMsgIds.mapIndexed { index, globalMsgId -> Pair(globalMsgId, localMsgIds[index]) }.toMap()
        return keyedStates.map { keyed ->
            val globalMsgId = keyed.globalMsgId
            val localMsgId = globalToLocalMap[globalMsgId]!!

            when (val state = keyed.item) {
                is MessageMapState.Unknown -> {
                    Keyed(globalMsgId, MessageMapState.Available(globalMsgId, localMsgId))
                }

                is MessageMapState.Partial -> {
                    val available = MessageMapState.Available(globalMsgId, localMsgId, state.rowId)
                    OnSmsSendStatusInAvailable(context).handle(Keyed(globalMsgId, available), state.smsSendStatus)
                    Log.d(TAG, "Processed status ${state.smsSendStatus.localMsgId} that was in Partial state")
                    Keyed(globalMsgId, available)
                }

                is MessageMapState.Available -> error("Event '${sendMirrored::class.simpleName}' not allowed when in state '${state::class.simpleName}'")
                is MessageMapState.Deleted -> error("Event '${sendMirrored::class.simpleName}' not allowed when in state '${state::class.simpleName}'")
            }
        }
    }
}
