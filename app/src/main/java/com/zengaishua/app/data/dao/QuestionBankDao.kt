package com.zengaishua.app.data.dao

import androidx.room.*
import com.zengaishua.app.data.model.QuestionBank
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionBankDao {
    @Query("SELECT * FROM question_banks ORDER BY importTime DESC")
    fun getAllBanks(): Flow<List<QuestionBank>>

    @Query("SELECT * FROM question_banks WHERE id = :bankId")
    suspend fun getBankById(bankId: String): QuestionBank?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBank(bank: QuestionBank)

    @Update
    suspend fun updateBank(bank: QuestionBank)

    @Delete
    suspend fun deleteBank(bank: QuestionBank)
}
