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
    val practiceMode: PracticeMode = PracticeMode.SEQUENTIAL
)

enum class PracticeMode {
    SEQUENTIAL,  // 顺序
    RANDOM       // 随机
}

class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ZasApplication).repository
    private val gson = Gson()

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        loadBanks()
    }

    private fun loadBanks() {
        viewModelScope.launch {
            repository.getAllBanks().collect { banks ->
                _uiState.update { it.copy(banks = banks) }
            }
        }
    }

    fun selectBank(bank: QuestionBank) {
        _uiState.update { it.copy(currentBank = bank) }
        loadQuestions(bank.id)
    }

    private fun loadQuestions(bankId: String) {
        viewModelScope.launch {
            repository.getQuestionsByBank(bankId).collect { questions ->
                val orderedQuestions = if (_uiState.value.practiceMode == PracticeMode.RANDOM) {
                    questions.shuffled()
                } else {
                    questions
                }
                _uiState.update {
                    it.copy(
                        questions = orderedQuestions,
                        currentQuestion = orderedQuestions.firstOrNull(),
                        currentIndex = 0
                    )
                }
            }
        }
    }

    fun loadFavorites(bankId: String) {
        viewModelScope.launch {
            repository.getFavoriteQuestions(bankId).collect { questions ->
                _uiState.update {
                    it.copy(
                        questions = questions,
                        currentQuestion = questions.firstOrNull(),
                        currentIndex = 0
                    )
                }
            }
        }
    }

    fun loadWrongQuestions(bankId: String) {
        viewModelScope.launch {
            repository.getWrongQuestions(bankId).collect { questions ->
                _uiState.update {
                    it.copy(
                        questions = questions,
                        currentQuestion = questions.firstOrNull(),
                        currentIndex = 0
                    )
                }
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

        if (currentIndex < questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentIndex = currentIndex + 1,
                    currentQuestion = questions[currentIndex + 1],
                    selectedAnswers = emptySet(),
                    showAnswer = false,
                    isCorrect = null
                )
            }
        }
    }

    fun toggleFavorite() {
        val question = _uiState.value.currentQuestion ?: return
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isFavorite = !question.isFavorite))
        }
    }

    fun setPracticeMode(mode: PracticeMode) {
        _uiState.update { it.copy(practiceMode = mode) }
        _uiState.value.currentBank?.let { loadQuestions(it.id) }
    }

    fun parseOptions(optionsJson: String): List<QuestionOption> {
        return try {
            gson.fromJson(optionsJson, Array<QuestionOption>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
