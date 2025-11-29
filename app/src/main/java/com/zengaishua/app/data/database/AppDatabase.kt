package com.zengaishua.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zengaishua.app.data.dao.LearningRecordDao
import com.zengaishua.app.data.dao.QuestionBankDao
import com.zengaishua.app.data.dao.QuestionDao
import com.zengaishua.app.data.model.LearningRecord
import com.zengaishua.app.data.model.Question
import com.zengaishua.app.data.model.QuestionBank

@Database(
    entities = [Question::class, QuestionBank::class, LearningRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun questionBankDao(): QuestionBankDao
    abstract fun learningRecordDao(): LearningRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zas_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
