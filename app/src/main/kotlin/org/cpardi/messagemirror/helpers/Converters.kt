package org.cpardi.messagemirror.helpers

import androidx.core.net.toUri
import androidx.room.TypeConverter
import org.cpardi.messagemirror.extensions.toLocalMsgId
import org.cpardi.messagemirror.extensions.toUri
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

class Converters {
    @TypeConverter
    fun fromGlobalMsgId(globalMsgId: GlobalMsgId): String = globalMsgId.value

    @TypeConverter
    fun toGlobalMsgId(value: String): GlobalMsgId = GlobalMsgId(value)

    @TypeConverter
    fun fromLocalMsgId(localMsgId: LocalMsgId): String = localMsgId.toUri().toString()

    @TypeConverter
    fun toLocalMsgId(value: String): LocalMsgId = value.toUri().toLocalMsgId()
}
