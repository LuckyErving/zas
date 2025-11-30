package com.zengaishua.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 题目类型
 */
enum class QuestionType(val value: Int) {
    SINGLE_CHOICE(1),      // 单选题
    MULTIPLE_CHOICE(2),    // 多选题
    TRUE_FALSE(3)          // 判断题
}

/**
 * 选项数据类
 */
data class QuestionOption(
    val tag: String,        // 选项标签 (A, B, C, D)
    val value: String       // 选项内容
)

/**
 * 题目实体
 */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey
    val id: String,                          // 题目ID
    val bankId: String,                      // 所属题库ID
    val stem: String,                        // 题干
    val type: Int,                           // 题目类型 (1=单选, 2=多选, 3=判断)
    val answer: String,                      // 正确答案 (如 "A" 或 "A,B")
    val optionsJson: String,                 // 选项JSON字符串
    val explanation: String = "",            // 解析
    var isFavorite: Boolean = false,         // 是否收藏
    var isWrong: Boolean = false,            // 是否在错题本
    var isCompleted: Boolean = false,        // 是否已完成
    var userAnswer: String = "",             // 用户答案
    var isCorrect: Boolean? = null,          // 是否答对
    var correctStreak: Int = 0               // 连续答对次数（错题需连续3次才剔除）
)

/**
 * 题库实体
 */
@Entity(tableName = "question_banks")
data class QuestionBank(
    @PrimaryKey
    val id: String,                          // 题库ID
    val name: String,                        // 题库名称
    val totalCount: Int,                     // 总题数
    val completedCount: Int = 0,             // 已完成题数
    val correctCount: Int = 0,               // 答对题数
    val lastPosition: Int = 0,               // 最后刷题位置
    val importTime: Long = System.currentTimeMillis() // 导入时间
)

/**
 * 学习记录实体
 */
@Entity(tableName = "learning_records")
data class LearningRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: String,                  // 题目ID
    val bankId: String,                      // 题库ID
    val userAnswer: String,                  // 用户答案
    val isCorrect: Boolean,                  // 是否正确
    val timestamp: Long = System.currentTimeMillis() // 答题时间
)
