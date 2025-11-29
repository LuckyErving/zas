package com.zengaishua.app.data.dao

import androidx.room.*
import com.zengaishua.app.data.model.LearningRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningRecordDao {
    @Query("SELECT * FROM learning_records WHERE bankId = :bankId ORDER BY timestamp DESC")
    fun getRecordsByBank(bankId: String): Flow<List<LearningRecord>>

    @Insert
    suspend fun insertRecord(record: LearningRecord)

    @Query("DELETE FROM learning_records WHERE bankId = :bankId")
    suspend fun deleteRecordsByBank(bankId: String)
}
