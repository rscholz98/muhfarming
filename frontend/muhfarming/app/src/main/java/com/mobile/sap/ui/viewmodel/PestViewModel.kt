package com.mobile.sap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sap.data.model.PestInfo
import com.mobile.sap.data.repository.PestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PestUiState {
    object Loading : PestUiState()
    data class Success(val pests: List<PestInfo>) : PestUiState()
    data class Error(val message: String) : PestUiState()
}

class PestViewModel : ViewModel() {
    private val repository = PestRepository()

    private val _uiState = MutableStateFlow<PestUiState>(PestUiState.Loading)
    val uiState: StateFlow<PestUiState> = _uiState.asStateFlow()

    init {
        loadPestInfo()
    }

    fun loadPestInfo() {
        viewModelScope.launch {
            _uiState.value = PestUiState.Loading
            repository.getPestInfo()
                .onSuccess { pests ->
                    _uiState.value = PestUiState.Success(pests)
                }
                .onFailure { exception ->
                    _uiState.value = PestUiState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }
}
