package org.cpardi.messagemirror.databases

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

@Entity
data class MessageMap(@PrimaryKey val globalMsgId: GlobalMsgId, val localMsgId: LocalMsgId)
