package com.zengaishua.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.zengaishua.app.data.dao.LearningRecordDao
import com.zengaishua.app.data.dao.QuestionBankDao
import com.zengaishua.app.data.dao.QuestionDao
import com.zengaishua.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val questionBankDao: QuestionBankDao,
    private val learningRecordDao: LearningRecordDao,
    private val context: Context
) {
    private val gson = Gson()

    // 获取所有题库
    fun getAllBanks(): Flow<List<QuestionBank>> = questionBankDao.getAllBanks()

    // 获取题库下的所有题目
    fun getQuestionsByBank(bankId: String): Flow<List<Question>> =
        questionDao.getQuestionsByBank(bankId)

    // 获取收藏的题目
    fun getFavoriteQuestions(bankId: String): Flow<List<Question>> =
        questionDao.getFavoriteQuestions(bankId)

    // 获取错题
    fun getWrongQuestions(bankId: String): Flow<List<Question>> =
        questionDao.getWrongQuestions(bankId)

    // 从JSON文件导入题目
    suspend fun importQuestionsFromJson(uri: Uri, bankName: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("无法打开文件"))

                val reader = InputStreamReader(inputStream, "UTF-8")
                val jsonData = gson.fromJson(reader, JsonQuestionData::class.java)
                reader.close()

                if (jsonData.status != 200) {
                    return@withContext Result.failure(Exception("JSON格式错误"))
                }

                // 生成唯一的题库ID：使用时间戳确保不会冲突
                val uniqueBankId = "bank_${System.currentTimeMillis()}"
                
                val questions = jsonData.obj.list.map { jsonQuestion ->
                    // 解析选项
                    val options = jsonQuestion.options.map { option ->
                        QuestionOption(
                            tag = option.tag,
                            value = option.value
                        )
                    }

                    // 为每个题目生成唯一ID：题库ID + 原题目ID
                    Question(
                        id = "${uniqueBankId}_${jsonQuestion.id}",
                        bankId = uniqueBankId,
                        stem = jsonQuestion.stemlist.firstOrNull()?.text ?: "",
                        type = jsonQuestion.type,
                        answer = jsonQuestion.answer,
                        optionsJson = gson.toJson(options),
                        explanation = jsonQuestion.jx ?: ""
                    )
                }

                // 保存到数据库
                val bank = QuestionBank(
                    id = uniqueBankId,
                    name = bankName,
                    totalCount = questions.size
                )
                questionBankDao.insertBank(bank)
                questionDao.insertQuestions(questions)

                Result.success("成功导入 ${questions.size} 道题目")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // 从assets导入默认题库
    suspend fun importDefaultQuestionBank(): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // 检查是否已有题库
                val existingBanks = questionBankDao.getAllBanksOnce()
                if (existingBanks.isNotEmpty()) {
                    return@withContext Result.success("已有题库，跳过导入")
                }

                val inputStream = context.assets.open("AI_level3.json")
                val reader = InputStreamReader(inputStream, "UTF-8")
                val jsonData = gson.fromJson(reader, JsonQuestionData::class.java)
                reader.close()

                if (jsonData.status != 200) {
                    return@withContext Result.failure(Exception("JSON格式错误"))
                }

                // 使用固定ID作为默认题库，确保首次安装的默认题库ID统一
                val defaultBankId = "bank_default_${jsonData.obj.id}"
                
                val questions = jsonData.obj.list.map { jsonQuestion ->
                    // 解析选项
                    val options = jsonQuestion.options.map { option ->
                        QuestionOption(
                            tag = option.tag,
                            value = option.value
                        )
                    }

                    Question(
                        id = "${defaultBankId}_${jsonQuestion.id}",
                        bankId = defaultBankId,
                        stem = jsonQuestion.stemlist.firstOrNull()?.text ?: "",
                        type = jsonQuestion.type,
                        answer = jsonQuestion.answer,
                        optionsJson = gson.toJson(options),
                        explanation = jsonQuestion.jx ?: ""
                    )
                }

                // 保存到数据库
                val bank = QuestionBank(
                    id = defaultBankId,
                    name = "默认题库",
                    totalCount = questions.size
                )
                questionBankDao.insertBank(bank)
                questionDao.insertQuestions(questions)

                Result.success("成功导入默认题库：${questions.size} 道题目")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // 更新题目 (收藏、答题等)
    suspend fun updateQuestion(question: Question) = withContext(Dispatchers.IO) {
        questionDao.updateQuestion(question)
    }

    // 提交答案
    suspend fun submitAnswer(
        question: Question,
        userAnswer: String
    ): Boolean = withContext(Dispatchers.IO) {
        // 判断答案是否正确
        val correctAnswerSet = question.answer.split(",").toSet()
        val userAnswerSet = userAnswer.split(",").toSet()
        val isCorrect = correctAnswerSet == userAnswerSet

        // 更新连续正确次数和错题状态
        val newCorrectStreak = if (isCorrect) {
            question.correctStreak + 1
        } else {
            0 // 答错则重置连续正确次数
        }

        // 如果题目在错题本中，需要连续答对3次才能移除
        val shouldRemoveFromWrong = question.isWrong && newCorrectStreak >= 3

        // 更新题目状态
        val updatedQuestion = question.copy(
            isCompleted = true,
            userAnswer = userAnswer,
            isCorrect = isCorrect,
            isWrong = if (shouldRemoveFromWrong) false else (question.isWrong || !isCorrect),
            correctStreak = newCorrectStreak
        )
        questionDao.updateQuestion(updatedQuestion)

        // 记录学习记录
        val record = LearningRecord(
            questionId = question.id,
            bankId = question.bankId,
            userAnswer = userAnswer,
            isCorrect = isCorrect
        )
        learningRecordDao.insertRecord(record)

        // 更新题库统计
        updateBankStats(question.bankId)

        isCorrect
    }

    // 更新题库统计信息
    private suspend fun updateBankStats(bankId: String) {
        val bank = questionBankDao.getBankById(bankId) ?: return
        val completedCount = questionDao.getCompletedCount(bankId)
        val correctCount = questionDao.getCorrectCount(bankId)

        questionBankDao.updateBank(
            bank.copy(
                completedCount = completedCount,
                correctCount = correctCount
            )
        )
    }

    // 导出学习数据
    suspend fun exportLearningData(bankId: String): String = withContext(Dispatchers.IO) {
        val questions = questionDao.getQuestionsByBank(bankId)
        // 这里简化处理,实际应该导出为JSON格式
        gson.toJson(questions)
    }

    // 删除题库
    suspend fun deleteBank(bank: QuestionBank) = withContext(Dispatchers.IO) {
        questionDao.deleteQuestionsByBank(bank.id)
        learningRecordDao.deleteRecordsByBank(bank.id)
        questionBankDao.deleteBank(bank)
    }

    // 获取题库信息
    suspend fun getBankById(bankId: String): QuestionBank? = withContext(Dispatchers.IO) {
        questionBankDao.getBankById(bankId)
    }

    // 更新题库刷题位置
    suspend fun updateBankPosition(bankId: String, position: Int) = withContext(Dispatchers.IO) {
        val bank = questionBankDao.getBankById(bankId) ?: return@withContext
        questionBankDao.updateBank(bank.copy(lastPosition = position))
    }
}
