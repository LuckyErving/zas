package com.zengaishua.app

import android.app.Application
import com.zengaishua.app.data.database.AppDatabase
import com.zengaishua.app.data.repository.QuestionRepository

class ZasApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        QuestionRepository(
            database.questionDao(),
            database.questionBankDao(),
            database.learningRecordDao(),
            this
        )
    }
}
