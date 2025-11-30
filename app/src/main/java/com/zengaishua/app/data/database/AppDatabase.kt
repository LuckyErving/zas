package com.zengaishua.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zengaishua.app.data.dao.LearningRecordDao
import com.zengaishua.app.data.dao.QuestionBankDao
import com.zengaishua.app.data.dao.QuestionDao
import com.zengaishua.app.data.model.LearningRecord
import com.zengaishua.app.data.model.Question
import com.zengaishua.app.data.model.QuestionBank

@Database(
    entities = [Question::class, QuestionBank::class, LearningRecord::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun questionBankDao(): QuestionBankDao
    abstract fun learningRecordDao(): LearningRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加lastPosition字段到question_banks表
                database.execSQL("ALTER TABLE question_banks ADD COLUMN lastPosition INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加correctStreak字段到questions表
                database.execSQL("ALTER TABLE questions ADD COLUMN correctStreak INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zas_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
