package com.zengaishua.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zengaishua.app.ZasApplication
import com.zengaishua.app.data.model.QuestionBank
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MineUiState(
    val banks: List<QuestionBank> = emptyList(),
    val selectedBank: QuestionBank? = null,
    val isExporting: Boolean = false,
    val exportResult: String? = null
)

class MineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ZasApplication).repository

    private val _uiState = MutableStateFlow(MineUiState())
    val uiState: StateFlow<MineUiState> = _uiState.asStateFlow()

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
        _uiState.update { it.copy(selectedBank = bank) }
    }

    fun exportData() {
        val bankId = _uiState.value.selectedBank?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val data = repository.exportLearningData(bankId)
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportResult = data
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportResult = null
                    )
                }
            }
        }
    }

    fun deleteBank(bank: QuestionBank) {
        viewModelScope.launch {
            repository.deleteBank(bank)
        }
    }
}
