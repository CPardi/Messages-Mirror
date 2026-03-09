package org.cpardi.messagemirror.databases

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.cpardi.messagemirror.models.GlobalMsgId
import org.cpardi.messagemirror.models.LocalMsgId

private val TAG: String = MessageMapDao::class.qualifiedName!!

@Dao
interface MessageMapDao {
    @Insert
    fun insert(message: MessageMapStateEntity)

    @Update
    fun update(message: MessageMapStateEntity)

    @Query("SELECT * FROM MessageMapStateEntity WHERE globalMsgId = :globalMsgId LIMIT 1")
    fun getByGlobalMsgId(globalMsgId: GlobalMsgId): MessageMapStateEntity?

    @Query("SELECT * FROM MessageMapStateEntity WHERE localMsgId = :localMsgId LIMIT 1")
    fun getByLocalMsgId(localMsgId: LocalMsgId): MessageMapStateEntity?

    @Query("DELETE FROM MessageMapStateEntity WHERE rowId = :rowId")
    fun deleteById(rowId: Long)
}

class LoggingMessageMapDao(val baseDao: MessageMapDao) : MessageMapDao {
    override fun insert(message: MessageMapStateEntity) {
        baseDao.insert(message)
        Log.d(TAG, "Inserted message map ${message.localMsgId}->${message.globalMsgId}")
    }

    override fun update(message: MessageMapStateEntity) {
        baseDao.update(message)
        Log.d(TAG, "Updated message map ${message.localMsgId}->${message.globalMsgId}")
    }

    override fun getByGlobalMsgId(globalMsgId: GlobalMsgId): MessageMapStateEntity? {
        val map = baseDao.getByGlobalMsgId(globalMsgId)
        if (map == null)
            Log.d(TAG, "Attempted to get message map for $globalMsgId, but does not exist")
        else
            Log.d(TAG, "Retrieved message map ${map.globalMsgId}->${map.localMsgId} using global msg Id")

        return map
    }

    override fun getByLocalMsgId(localMsgId: LocalMsgId): MessageMapStateEntity? {
        val map = baseDao.getByLocalMsgId(localMsgId)
        if (map == null)
            Log.d(TAG, "Attempted to get message map for $localMsgId, but does not exist")
        else
            Log.d(TAG, "Retrieved message map ${map.localMsgId}->${map.globalMsgId} using local msg Id")

        return map
    }

    override fun deleteById(rowId: Long) {
        baseDao.deleteById(rowId)
        Log.d(TAG, "Deleted message map with row id $rowId")
    }
}
