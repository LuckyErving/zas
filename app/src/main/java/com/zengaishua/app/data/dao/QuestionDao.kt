package com.zengaishua.app.data.dao

import androidx.room.*
import com.zengaishua.app.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE bankId = :bankId")
    fun getQuestionsByBank(bankId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE bankId = :bankId AND isFavorite = 1")
    fun getFavoriteQuestions(bankId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE bankId = :bankId AND isWrong = 1")
    fun getWrongQuestions(bankId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: String): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Query("DELETE FROM questions WHERE bankId = :bankId")
    suspend fun deleteQuestionsByBank(bankId: String)

    @Query("SELECT COUNT(*) FROM questions WHERE bankId = :bankId")
    suspend fun getQuestionCount(bankId: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE bankId = :bankId AND isCompleted = 1")
    suspend fun getCompletedCount(bankId: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE bankId = :bankId AND isCorrect = 1")
    suspend fun getCorrectCount(bankId: String): Int
}
