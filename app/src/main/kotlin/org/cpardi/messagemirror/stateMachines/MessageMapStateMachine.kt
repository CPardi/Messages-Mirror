package org.cpardi.messagemirror.stateMachines

import android.content.Context
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.cpardi.messagemirror.databases.LoggingMessageMapDao
import org.cpardi.messagemirror.databases.MessageMapDao
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.databases.MirrorDatabase
import org.cpardi.messagemirror.extensions.toEntity
import org.cpardi.messagemirror.extensions.toState
import org.cpardi.messagemirror.stateMachines.handlers.OnAnyEventPost
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsPartReceiveInUnknown
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsReceiveInUnknown
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsReceiveMirroredInUnknown
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendInMultipleStates
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendMirroredInMultipleStates
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendStatusInAvailable
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendStatusInUnknown
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendStatusMirroredInAvailable
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendStatusMirroredInUnknown
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.LKeyed
import org.cpardi.messagemirror.models.LocalMsgId
import org.cpardi.messagemirror.models.StateType

private val TAG: String = MessageMapStateMachine::class.qualifiedName!!

class MessageMapStateMachine(val context: Context) {
    companion object {
        private val singleThreadDispatcher = @OptIn(ExperimentalCoroutinesApi::class)newSingleThreadContext("MessageMapStateMachineThread")
        private var DaoInstance: MessageMapDao? = null
    }

    private val dao: MessageMapDao
        get() = DaoInstance ?: LoggingMessageMapDao(MirrorDatabase.Holder.getInstance(context).MessageMapDao())

    private val onSmsReceiveInUnknown = OnSmsReceiveInUnknown(context)
    private val onSmsReceiveMirroredInUnknown = OnSmsReceiveMirroredInUnknown(context)

    private val onSmsPartReceiveInUnknown = OnSmsPartReceiveInUnknown(context)

    private val onSmsSendInMultipleStates = OnSmsSendInMultipleStates(context)
    private val onSmsSendMirroredInMultipleStates = OnSmsSendMirroredInMultipleStates(context)

    private val onSmsSendStatusInUnknown = OnSmsSendStatusInUnknown(context)
    private val onSmsSendStatusInAvailable = OnSmsSendStatusInAvailable(context)

    private val onSmsSendStatusMirroredInUnknown = OnSmsSendStatusMirroredInUnknown(context)
    private val onSmsSendStatusMirroredInAvailable = OnSmsSendStatusMirroredInAvailable(context)

    private val onAnyEventPost = OnAnyEventPost(context)

    fun process(dto: EventDto) = runBlocking(@OptIn(ExperimentalCoroutinesApi::class)singleThreadDispatcher) {
        Log.d(TAG, "State machine started processing ${dto::class.simpleName} event")
        when (dto) {
            is EventDto.SmsReceive -> onSmsReceive(dto)
            is EventDto.SmsPartReceive -> onSmsPartReceive(dto)
            is EventDto.SmsReceiveMirrored -> onSmsReceiveMirrored(dto)

            is EventDto.SmsSend -> onSmsSend(dto)
            is EventDto.SmsSendMirrored -> onSmsSendMirrored(dto)

            is EventDto.SmsSendStatus -> onSmsSendStatus(dto)
            is EventDto.SmsSendStatusMirrored -> onSmsSendStatusMirrored(dto)
        }

        onAnyEventPost.handle(dto)
        Log.d(TAG, "State machine finished processing ${dto::class.simpleName} event")
    }

    private fun onSmsReceive(receive: EventDto.SmsReceive) {
        updateStates(onSmsReceiveInUnknown.handle(receive))
    }

    private fun onSmsPartReceive(receive: EventDto.SmsPartReceive) {
        updateState(onSmsPartReceiveInUnknown.handle(receive))
    }

    private fun onSmsReceiveMirrored(receive: EventDto.SmsReceiveMirrored) {
        updateStates(onSmsReceiveMirroredInUnknown.handle(receive))
    }

    private fun onSmsSend(send: EventDto.SmsSend) {
        updateStates(onSmsSendInMultipleStates.handle(send))
    }

    private fun onSmsSendMirrored(sendMirrored: EventDto.SmsSendMirrored) {
        val states = sendMirrored.globalMsgIds.map { Keyed(it, getCurrentState(it)) }
        updateStates(onSmsSendMirroredInMultipleStates.handle(states, sendMirrored))
    }

    private fun onSmsSendStatus(status: EventDto.SmsSendStatus) {
        val state = getCurrentState(status.localMsgId)
        when (state) {
            is MessageMapState.Unknown -> updateState(onSmsSendStatusInUnknown.handle(status))
            is MessageMapState.Partial -> state.disallowed(status)
            is MessageMapState.Available -> updateState(onSmsSendStatusInAvailable.handle(Keyed(state.globalMsgId, state), status))
        }
    }

    private fun onSmsSendStatusMirrored(status: EventDto.SmsSendStatusMirrored) {
        val keyedState = Keyed(status.globalMsgId, getCurrentState(status.globalMsgId))
        when (val state = keyedState.item) {
            is MessageMapState.Unknown -> onSmsSendStatusMirroredInUnknown.handle(status)
            is MessageMapState.Partial -> state.disallowed(status)
            is MessageMapState.Available -> onSmsSendStatusMirroredInAvailable.handle(Keyed(keyedState.globalMsgId, state), status)
        }.let { updateState(it) }
    }

    private fun getCurrentState(localMsgId: LocalMsgId): MessageMapState {
        val state = dao.getByLocalMsgId(localMsgId).toState()
        Log.d(TAG, "State machine ${localMsgId}, is in ${state::class.simpleName}")
        return state
    }

    private fun getCurrentState(globalMsgId: GlobalMsgId): MessageMapState {
        val state = dao.getByGlobalMsgId(globalMsgId).toState()

        Log.d(TAG, "State machine ${globalMsgId}, is in ${state::class.simpleName}")
        return state
    }

    private fun updateState(state: Keyed<MessageMapState>?) {
        if (state == null)
            return

        val entity = state.item.toEntity(state.globalMsgId)
        if (entity.stateType != StateType.Unknown)
            dao.upsert(entity)

        Log.d(TAG, "State machine ${state.globalMsgId}, transition to ${state.item::class.simpleName}")
    }

    private fun updateState(keyedState: LKeyed<MessageMapState>?) {
        if (keyedState == null)
            return

        val entity = keyedState.item.toEntity(keyedState.localMsgId)
        if (entity.stateType != StateType.Unknown)
            dao.upsert(entity)

        Log.d(TAG, "State machine ${keyedState.localMsgId}, transition to ${keyedState.item::class.simpleName}")
    }

    private fun updateStates(states: List<Keyed<MessageMapState>>?) {
        if(states?.isEmpty() ?: false)
            Log.w(TAG, "Attempted to update 0 states")

        states?.forEach { updateState(it) }
    }

    private fun MessageMapState.Partial.disallowed(dto: EventDto): Keyed<MessageMapState.Partial>? {
        Log.w(TAG, "State machine ${this.smsSendStatus.localMsgId}, event '${dto::class.simpleName}' is not allowed when in state '${this::class.simpleName}'")
        return null
    }

    private fun MessageMapState.Available.disallowed(dto: EventDto): Keyed<MessageMapState.Available>? {
        Log.w(TAG, "State machine ${this.globalMsgId}, event '${dto::class.simpleName}' is not allowed when in state '${this::class.simpleName}'")
        return null
    }
}
