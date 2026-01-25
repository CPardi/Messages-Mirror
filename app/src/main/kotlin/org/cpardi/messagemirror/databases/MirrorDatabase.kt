package org.cpardi.messagemirror.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.cpardi.messagemirror.helpers.Converters

@Database(entities = [MessageMapStateEntity::class], version = 8)
@TypeConverters(Converters::class)
abstract class MirrorDatabase : RoomDatabase() {
    abstract fun MessageMapDao(): MessageMapDao

    companion object Holder {
        @Volatile
        private var INSTANCE: MirrorDatabase? = null

        fun getInstance(context: Context): MirrorDatabase {
            return synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = MirrorDatabase::class.java,
                    name = "mirror_state.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
