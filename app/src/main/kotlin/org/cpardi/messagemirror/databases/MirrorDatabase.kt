package org.cpardi.messagemirror.databases

import android.content.Context
import androidx.room.*

@Database(entities = [MessageMap::class], version = 3)
@TypeConverters(Converters::class)
abstract class MirrorDatabase : RoomDatabase() {
    abstract fun MessageMapDao(): MessageMapDao

    companion object Holder {
        @Volatile
        private var INSTANCE: MirrorDatabase? = null

        fun getInstance(context: Context): MirrorDatabase {
            return INSTANCE ?: synchronized(this) {
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
