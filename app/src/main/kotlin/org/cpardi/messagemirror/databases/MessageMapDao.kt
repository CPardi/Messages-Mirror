package org.cpardi.messagemirror.databases

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

private val TAG: String = MessageMapDao::class.qualifiedName!!

@Dao
interface MessageMapDao {
    @Insert
    fun insert(message: MessageMap)

    @Query("SELECT * FROM MessageMap WHERE globalMsgId = :globalMsgId")
    fun getByGlobalMsgId(globalMsgId: GlobalMsgId): MessageMap

    @Query("SELECT * FROM MessageMap WHERE localMsgId = :localMsgId LIMIT 1")
    fun getByLocalMsgId(localMsgId: LocalMsgId): MessageMap
}

class LoggingMessageMapDao(val baseDao: MessageMapDao) : MessageMapDao {
    override fun insert(map: MessageMap) {
        baseDao.insert(map)
        Log.d(TAG, "Inserted message map ${map.localMsgId}->${map.globalMsgId}")
    }

    override fun getByGlobalMsgId(globalMsgId: GlobalMsgId): MessageMap {
        val map = baseDao.getByGlobalMsgId(globalMsgId)
        Log.d(TAG, "Retrieved message map ${map.globalMsgId}->${map.localMsgId} using global msg Id")
        return map
    }

    override fun getByLocalMsgId(localMsgId: LocalMsgId): MessageMap {
        val map = baseDao.getByLocalMsgId(localMsgId)
        Log.d(TAG, "Retrieved message map ${map.localMsgId}->${map.globalMsgId} using local msg Id")
        return map
    }

}
