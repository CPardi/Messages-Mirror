package org.cpardi.messagemirror.stateMachines.handlers

import android.content.Context
import org.cpardi.messagemirror.databases.MessageMapState
import org.cpardi.messagemirror.extensions.mirrorConfig
import org.cpardi.messagemirror.extensions.toGlobalMsgId
import org.cpardi.messagemirror.models.EventDto
import org.cpardi.messagemirror.models.GLKeyed

class OnSmsSendStatusInPartial(val context: Context) {
    fun handle(dto: EventDto.SmsSendStatus): GLKeyed<MessageMapState>? {
        val localMsgId = dto.updatedLocalMsgId ?: dto.localMsgId
        return GLKeyed(localMsgId, localMsgId.toGlobalMsgId(context.mirrorConfig.deviceID), MessageMapState.Partial(dto))
    }
}
