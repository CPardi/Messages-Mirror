package org.cpardi.messagemirror.stateMachines

import android.content.Context
import android.util.Log
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.cpardi.messagemirror.databases.LoggingMessageMapDao
import org.cpardi.messagemirror.databases.MessageMapDao
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.databases.MessageMapStateEntity
import org.cpardi.messagemirror.databases.MirrorDatabase
import org.cpardi.messagemirror.extensions.toEntity
import org.cpardi.messagemirror.extensions.toState
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
import org.cpardi.messagemirror.models.GLKeyed
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.Keyed
import org.cpardi.messagemirror.models.LKeyed
import org.cpardi.messagemirror.models.LocalMsgId
import org.cpardi.messagemirror.models.StateType
import org.cpardi.messagemirror.stateMachines.handlers.OnDeleteSmsInAvailable
import org.cpardi.messagemirror.stateMachines.handlers.OnDeleteSmsInUnknown
import org.cpardi.messagemirror.stateMachines.handlers.OnDeleteSmsMirroredInAvailable
import org.cpardi.messagemirror.stateMachines.handlers.OnDeleteSmsMirroredInUnknown
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendStatusInPartial
import org.cpardi.messagemirror.stateMachines.handlers.OnSmsSendStatusMirroredInPartial
import org.fossify.commons.helpers.ensureBackgroundThread
import java.util.concurrent.Executors

private val TAG: String = MessageMapStateMachine::class.qualifiedName!!

class MessageMapStateMachine(val context: Context) {
    companion object {
        private val singleThreadExecutor = Executors.newSingleThreadExecutor()
        private val singleThreadDispatcher: ExecutorCoroutineDispatcher = singleThreadExecutor.asCoroutineDispatcher()
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
    private val onSmsSendStatusInPartial = OnSmsSendStatusInPartial(context)
    private val onSmsSendStatusInAvailable = OnSmsSendStatusInAvailable(context)

    private val onSmsSendStatusMirroredInUnknown = OnSmsSendStatusMirroredInUnknown(context)
    private val onSmsSendStatusMirroredInPartial = OnSmsSendStatusMirroredInPartial(context)
    private val onSmsSendStatusMirroredInAvailable = OnSmsSendStatusMirroredInAvailable(context)

    private val onDeleteSmsInUnknown = OnDeleteSmsInUnknown(context)
    private val onDeleteSmsInAvailable = OnDeleteSmsInAvailable(context)

    private val onDeleteSmsMirroredInUnknown = OnDeleteSmsMirroredInUnknown(context)
    private val onDeleteSmsMirroredInAvailable = OnDeleteSmsMirroredInAvailable(context)

    fun processBackground(dto: EventDto) = ensureBackgroundThread {
        processBlocking(dto)
    }

    fun processBlocking(dto: EventDto) = runBlocking(singleThreadDispatcher) {
        processInternal(dto)
    }

    private fun processInternal(dto: EventDto) {
        Log.d(TAG, "State machine started processing ${dto::class.simpleName} event")
        when (dto) {
            is EventDto.SmsReceive -> onSmsReceive(dto)
            is EventDto.SmsPartReceive -> onSmsPartReceive(dto)
            is EventDto.SmsReceiveMirrored -> onSmsReceiveMirrored(dto)

            is EventDto.SmsSend -> onSmsSend(dto)
            is EventDto.SmsSendMirrored -> onSmsSendMirrored(dto)

            is EventDto.SmsSendStatus -> onSmsSendStatus(dto)
            is EventDto.SmsSendStatusMirrored -> onSmsSendStatusMirrored(dto)

            is EventDto.DeleteSms -> onDeleteSms(dto)
            is EventDto.DeleteSmsMirrored -> onDeleteSmsMirrored(dto)

            is EventDto.LocalMsgIdUpdated -> onLocalMsgIdUpdated(dto)
        }

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
        updateState(onSmsSendInMultipleStates.handle(send))
    }

    private fun onSmsSendMirrored(sendMirrored: EventDto.SmsSendMirrored) {
        val state = Keyed(sendMirrored.globalMsgId, getCurrentState(sendMirrored.globalMsgId))
        updateState(onSmsSendMirroredInMultipleStates.handle(state, sendMirrored))
    }

    private fun onSmsSendStatus(status: EventDto.SmsSendStatus) {
        val state = getCurrentState(status.localMsgId)
        when (state) {
            is MessageMapState.Unknown -> updateState(onSmsSendStatusInUnknown.handle(status))
            is MessageMapState.Partial -> updateState(onSmsSendStatusInPartial.handle(status))
            is MessageMapState.Available -> updateState(onSmsSendStatusInAvailable.handle(Keyed(state.globalMsgId, state), status))
            is MessageMapState.Deleted -> state.disallowed(status)
        }
    }

    private fun onSmsSendStatusMirrored(status: EventDto.SmsSendStatusMirrored) {
        val keyedState = Keyed(status.globalMsgId, getCurrentState(status.globalMsgId))
        when (val state = keyedState.item) {
            is MessageMapState.Unknown -> updateState(onSmsSendStatusMirroredInUnknown.handle(status))
            is MessageMapState.Partial -> updateState(onSmsSendStatusMirroredInPartial.handle(state, status))
            is MessageMapState.Available -> updateState(onSmsSendStatusMirroredInAvailable.handle(Keyed(keyedState.globalMsgId, state), status))
            is MessageMapState.Deleted -> state.disallowed(status)
        }
    }

    private fun onDeleteSms(delete: EventDto.DeleteSms) {
        val keyedState = LKeyed(delete.localMsgId, getCurrentState(delete.localMsgId))
        when (val state = keyedState.item) {
            is MessageMapState.Unknown -> updateState(onDeleteSmsInUnknown.handle(delete))
            is MessageMapState.Partial -> state.disallowed(delete)
            is MessageMapState.Available -> updateState(onDeleteSmsInAvailable.handle(state, delete))
            is MessageMapState.Deleted -> state.disallowed(delete)
        }
    }

    private fun onDeleteSmsMirrored(delete: EventDto.DeleteSmsMirrored) {
        val keyedState = Keyed(delete.globalMsgId, getCurrentState(delete.globalMsgId))
        when (val state = keyedState.item) {
            is MessageMapState.Unknown -> updateState(onDeleteSmsMirroredInUnknown.handle(delete))
            is MessageMapState.Partial -> state.disallowed(delete)
            is MessageMapState.Available -> updateState(onDeleteSmsMirroredInAvailable.handle(state, delete))
            is MessageMapState.Deleted -> state.disallowed(delete)
        }
    }

    private fun onLocalMsgIdUpdated(dto: EventDto.LocalMsgIdUpdated) {
        val state = getCurrentState(dto.localMsgId)
        updateState(LKeyed(dto.updatedLocalMsgId, state))
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
        if (state == null) return
        val entity = state.item.toEntity(state.globalMsgId)
        updateEntityState(entity, state.globalMsgId.toString(), state.item::class.simpleName)
    }

    private fun updateState(keyedState: LKeyed<MessageMapState>?) {
        if (keyedState == null) return
        val entity = keyedState.item.toEntity(keyedState.localMsgId)
        updateEntityState(entity, keyedState.localMsgId.toString(), keyedState.item::class.simpleName)
    }

    private fun updateState(keyedState: GLKeyed<MessageMapState>?) {
        if (keyedState == null) return
        val entity = keyedState.item.toEntity(keyedState.localMsgId, keyedState.globalMsgId)
        updateEntityState(entity, "${keyedState.localMsgId}/${keyedState.globalMsgId}", keyedState.item::class.simpleName)
    }

    private fun updateEntityState(entity: MessageMapStateEntity, id: String, stateName: String?) {
        when (entity.stateType) {
            StateType.Deleted -> dao.deleteById(entity.rowId)
            StateType.Unknown -> {} // Do nothing for Unknown state
            else ->if(entity.rowId == 0L) dao.insert(entity) else dao.update(entity)
        }

        Log.d(TAG, "State machine $id, transition to $stateName")
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

    private fun MessageMapState.Deleted.disallowed(dto: EventDto): Keyed<MessageMapState.Deleted>? {
        Log.w(TAG, "State machine ${this.rowId}, event '${dto::class.simpleName}' is not allowed when in state '${this::class.simpleName}'")
        return null
    }
}
