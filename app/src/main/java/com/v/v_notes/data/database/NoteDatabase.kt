package com.v.v_notes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.v.v_notes.data.converters.Converters
import com.v.v_notes.data.dao.NoteDao
import com.v.v_notes.data.model.Note

@Database(
    entities = [Note::class],
    version = 1, // 数据库版本，结构改变时需要升级
    exportSchema = false
)
@TypeConverters(Converters::class) // 注册类型转换器
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    // 使用单例模式防止数据库实例被重复打开
    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}