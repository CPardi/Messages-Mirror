package org.cpardi.messagemirror.databases

import androidx.room.TypeConverter
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

class Converters {
    @TypeConverter
    fun fromGlobalMsgId(globalMsgId: GlobalMsgId): String = globalMsgId.value

    @TypeConverter
    fun toGlobalMsgId(value: String): GlobalMsgId = GlobalMsgId(value)

    @TypeConverter
    fun fromLocalMsgId(localMsgId: LocalMsgId): String = localMsgId.value

    @TypeConverter
    fun toLocalMsgId(value: String): LocalMsgId = LocalMsgId(value)
}
