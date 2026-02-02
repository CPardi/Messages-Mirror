package org.cpardi.messagemirror.extensions

import kotlinx.serialization.json.Json
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.databases.MessageMapStateEntity
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId
import org.cpardi.messagemirror.models.StateType

fun MessageMapState.toEntity(globalMsgId: GlobalMsgId): MessageMapStateEntity = when (this) {
    is MessageMapState.Unknown -> MessageMapStateEntity(
        stateType = StateType.Unknown,
        globalMsgId = globalMsgId,
    )

    is MessageMapState.Partial -> MessageMapStateEntity(
        stateType = StateType.Partial,
        globalMsgId = globalMsgId,
        smsSendStatusJson = Json.encodeToString(smsSendStatus),
        rowId = this.rowId ?: 0
    )

    is MessageMapState.Available -> MessageMapStateEntity(
        stateType = StateType.Available,
        globalMsgId = globalMsgId,
        localMsgId = localMsgId,
        rowId = this.rowId ?: 0
    )

    is MessageMapState.Deleted -> MessageMapStateEntity(
        stateType = StateType.Deleted,
        globalMsgId = globalMsgId,
        rowId = rowId ?: error("rowId field must always be set in the Deleted state")
    )
}

fun MessageMapState.toEntity(localMsgId: LocalMsgId): MessageMapStateEntity = when (this) {
    is MessageMapState.Unknown -> MessageMapStateEntity(
        stateType = StateType.Unknown,
        localMsgId = localMsgId,
    )

    is MessageMapState.Partial -> MessageMapStateEntity(
        stateType = StateType.Partial,
        localMsgId = localMsgId,
        smsSendStatusJson = Json.encodeToString(smsSendStatus),
        rowId = this.rowId ?: 0
    )

    is MessageMapState.Available -> MessageMapStateEntity(
        stateType = StateType.Available,
        localMsgId = localMsgId,
        globalMsgId = this.globalMsgId,
        rowId = this.rowId ?: 0
    )

    is MessageMapState.Deleted -> MessageMapStateEntity(
        stateType = StateType.Deleted,
        localMsgId = localMsgId,
        rowId = rowId ?: error("rowId field must always be set in the Deleted state")
    )
}

fun MessageMapStateEntity?.toState(): MessageMapState =
    if (this == null)
        MessageMapState.Unknown()
    else
        when (stateType) {
            StateType.Partial -> {
                val status = smsSendStatusJson?.let { Json.decodeFromString<EventDto.SmsSendStatus>(it) }
                if (status != null) MessageMapState.Partial(status, rowId)
                else error("Invalid Partial state JSON")
            }

            StateType.Available -> {
                if (localMsgId != null) {
                    MessageMapState.Available(globalMsgId!!, localMsgId, rowId)
                } else {
                    error("Available state missing localMsgId")
                }
            }

            StateType.Deleted -> {
                MessageMapState.Deleted(rowId)
            }

            else -> throw IllegalArgumentException("Unknown stateType: $stateType")
        }
