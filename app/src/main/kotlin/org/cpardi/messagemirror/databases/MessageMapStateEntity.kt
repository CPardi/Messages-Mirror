package org.cpardi.messagemirror.databases

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId
import org.cpardi.messagemirror.models.StateType

@Entity
data class MessageMapStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stateType: StateType,
    val globalMsgId: GlobalMsgId? = null,
    val localMsgId: LocalMsgId? = null,
    val smsSendStatusJson: String? = null,
)
