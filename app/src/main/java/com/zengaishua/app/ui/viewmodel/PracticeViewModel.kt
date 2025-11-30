package com.zengaishua.app.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.zengaishua.app.ZasApplication
import com.zengaishua.app.data.model.Question
import com.zengaishua.app.data.model.QuestionBank
import com.zengaishua.app.data.model.QuestionOption
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PracticeUiState(
    val banks: List<QuestionBank> = emptyList(),
    val currentBank: QuestionBank? = null,
    val questions: List<Question> = emptyList(),
    val currentQuestion: Question? = null,
    val currentIndex: Int = 0,
    val selectedAnswers: Set<String> = emptySet(),
    val showAnswer: Boolean = false,
    val isCorrect: Boolean? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val practiceMode: PracticeMode = PracticeMode.SEQUENTIAL,
    val studyMode: StudyMode = StudyMode.PRACTICE
)

enum class PracticeMode {
    SEQUENTIAL,  // 顺序
    RANDOM       // 随机
}

enum class StudyMode {
    PRACTICE,    // 练习模式
    MEMORIZE     // 背题模式
}

class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ZasApplication).repository
    private val gson = Gson()

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        loadBanks()
        importDefaultBankIfNeeded()
    }

    private fun loadBanks() {
        viewModelScope.launch {
            repository.getAllBanks().collect { banks ->
                _uiState.update { it.copy(banks = banks) }
            }
        }
    }

    private fun importDefaultBankIfNeeded() {
        viewModelScope.launch {
            repository.importDefaultQuestionBank()
        }
    }

    fun selectBank(bank: QuestionBank) {
        _uiState.update { it.copy(currentBank = bank) }
        loadQuestions(bank.id)
    }

    private fun loadQuestions(bankId: String) {
        viewModelScope.launch {
            val bank = repository.getBankById(bankId)
            // 使用first()而不是collect()，只获取一次数据
            val questions = repository.getQuestionsByBank(bankId).first()
            val orderedQuestions = if (_uiState.value.practiceMode == PracticeMode.RANDOM) {
                questions.shuffled()
            } else {
                questions
            }
            // 恢复上次刷题位置
            val startIndex = bank?.lastPosition?.coerceIn(0, orderedQuestions.size - 1) ?: 0
            val startQuestion = orderedQuestions.getOrNull(startIndex)
            _uiState.update {
                it.copy(
                    questions = orderedQuestions,
                    currentQuestion = startQuestion,
                    currentIndex = startIndex,
                    selectedAnswers = if (startQuestion?.isCompleted == true) 
                        startQuestion.userAnswer.split(",").filter { it.isNotBlank() }.toSet() 
                    else emptySet(),
                    showAnswer = startQuestion?.isCompleted == true,
                    isCorrect = startQuestion?.isCorrect
                )
            }
        }
    }

    fun loadFavorites(bankId: String) {
        viewModelScope.launch {
            // 使用first()而不是collect()，只获取一次数据
            val questions = repository.getFavoriteQuestions(bankId).first()
            val firstQuestion = questions.firstOrNull()
            _uiState.update {
                it.copy(
                    questions = questions,
                    currentQuestion = firstQuestion,
                    currentIndex = 0,
                    selectedAnswers = if (firstQuestion?.isCompleted == true) 
                        firstQuestion.userAnswer.split(",").filter { it.isNotBlank() }.toSet() 
                    else emptySet(),
                    showAnswer = firstQuestion?.isCompleted == true,
                    isCorrect = firstQuestion?.isCorrect
                )
            }
        }
    }

    fun loadWrongQuestions(bankId: String) {
        viewModelScope.launch {
            // 使用first()而不是collect()，只获取一次数据
            val questions = repository.getWrongQuestions(bankId).first()
            val firstQuestion = questions.firstOrNull()
            _uiState.update {
                it.copy(
                    questions = questions,
                    currentQuestion = firstQuestion,
                    currentIndex = 0,
                    selectedAnswers = if (firstQuestion?.isCompleted == true) 
                        firstQuestion.userAnswer.split(",").filter { it.isNotBlank() }.toSet() 
                    else emptySet(),
                    showAnswer = firstQuestion?.isCompleted == true,
                    isCorrect = firstQuestion?.isCorrect
                )
            }
        }
    }

    fun importQuestions(uri: Uri, bankName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.importQuestionsFromJson(uri, bankName)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun toggleAnswer(answer: String) {
        val current = _uiState.value.selectedAnswers.toMutableSet()
        val question = _uiState.value.currentQuestion ?: return

        when (question.type) {
            1, 3 -> { // 单选或判断题
                current.clear()
                current.add(answer)
            }
            2 -> { // 多选题
                if (current.contains(answer)) {
                    current.remove(answer)
                } else {
                    current.add(answer)
                }
            }
        }
        _uiState.update { it.copy(selectedAnswers = current) }
    }

    fun submitAnswer() {
        val question = _uiState.value.currentQuestion ?: return
        val userAnswer = _uiState.value.selectedAnswers.sorted().joinToString(",")

        viewModelScope.launch {
            val isCorrect = repository.submitAnswer(question, userAnswer)
            _uiState.update {
                it.copy(
                    showAnswer = true,
                    isCorrect = isCorrect
                )
            }
        }
    }

    fun nextQuestion() {
        val questions = _uiState.value.questions
        val currentIndex = _uiState.value.currentIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < questions.size) {
            val nextQuestion = questions[nextIndex]
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    currentQuestion = nextQuestion,
                    selectedAnswers = if (nextQuestion.isCompleted) 
                        nextQuestion.userAnswer.split(",").filter { it.isNotBlank() }.toSet() 
                    else emptySet(),
                    showAnswer = nextQuestion.isCompleted,
                    isCorrect = nextQuestion.isCorrect
                )
            }
            saveCurrentPosition()
        }
    }
    
    fun previousQuestion() {
        val questions = _uiState.value.questions
        val currentIndex = _uiState.value.currentIndex
        val prevIndex = currentIndex - 1

        if (prevIndex >= 0) {
            val prevQuestion = questions[prevIndex]
            _uiState.update {
                it.copy(
                    currentIndex = prevIndex,
                    currentQuestion = prevQuestion,
                    selectedAnswers = if (prevQuestion.isCompleted) 
                        prevQuestion.userAnswer.split(",").filter { it.isNotBlank() }.toSet() 
                    else emptySet(),
                    showAnswer = prevQuestion.isCompleted,
                    isCorrect = prevQuestion.isCorrect
                )
            }
            saveCurrentPosition()
        }
    }
    
    fun goToQuestion(index: Int) {
        val questions = _uiState.value.questions
        if (index in questions.indices) {
            val targetQuestion = questions[index]
            _uiState.update {
                it.copy(
                    currentIndex = index,
                    currentQuestion = targetQuestion,
                    selectedAnswers = if (targetQuestion.isCompleted) 
                        targetQuestion.userAnswer.split(",").filter { it.isNotBlank() }.toSet() 
                    else emptySet(),
                    showAnswer = targetQuestion.isCompleted,
                    isCorrect = targetQuestion.isCorrect
                )
            }
            saveCurrentPosition()
        }
    }

    private fun saveCurrentPosition() {
        val bank = _uiState.value.currentBank ?: return
        val currentIndex = _uiState.value.currentIndex
        viewModelScope.launch {
            repository.updateBankPosition(bank.id, currentIndex)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 保存当前位置
        saveCurrentPosition()
    }

    fun toggleFavorite() {
        val question = _uiState.value.currentQuestion ?: return
        val questions = _uiState.value.questions.toMutableList()
        val currentIndex = _uiState.value.currentIndex
        
        // 更新当前题目的收藏状态
        val updatedQuestion = question.copy(isFavorite = !question.isFavorite)
        questions[currentIndex] = updatedQuestion
        
        // 立即更新UI状态
        _uiState.update {
            it.copy(
                currentQuestion = updatedQuestion,
                questions = questions
            )
        }
        
        // 异步更新数据库
        viewModelScope.launch {
            repository.updateQuestion(updatedQuestion)
        }
    }

    fun setPracticeMode(mode: PracticeMode) {
        _uiState.update { it.copy(practiceMode = mode) }
        _uiState.value.currentBank?.let { loadQuestions(it.id) }
    }

    fun setStudyMode(mode: StudyMode) {
        _uiState.update {
            it.copy(
                studyMode = mode,
                selectedAnswers = emptySet(),
                showAnswer = false,
                isCorrect = null
            )
        }
    }

    fun previousQuestion() {
        val currentIndex = _uiState.value.currentIndex
        if (currentIndex > 0) {
            val questions = _uiState.value.questions
            _uiState.update {
                it.copy(
                    currentIndex = currentIndex - 1,
                    currentQuestion = questions[currentIndex - 1],
                    selectedAnswers = emptySet(),
                    showAnswer = false,
                    isCorrect = null
                )
            }
            saveCurrentPosition()
        }
    }

    fun parseOptions(optionsJson: String): List<QuestionOption> {
        return try {
            gson.fromJson(optionsJson, Array<QuestionOption>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
