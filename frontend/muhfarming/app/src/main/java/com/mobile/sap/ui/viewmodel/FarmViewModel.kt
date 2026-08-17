package com.mobile.sap.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sap.data.api.dto.FarmDto
import com.mobile.sap.data.repository.FarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FarmUiState {
    object Loading : FarmUiState()
    data class Success(val farms: List<FarmDto>) : FarmUiState()
    data class Error(val message: String) : FarmUiState()
}

class FarmViewModel(
    private val repository: FarmRepository = FarmRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FarmUiState>(FarmUiState.Loading)
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    init {
        loadFarms()
    }

    fun loadFarms() {
        viewModelScope.launch {
            _uiState.value = FarmUiState.Loading
            repository.list().fold(
                onSuccess = { _uiState.value = FarmUiState.Success(it) },
                onFailure = { e ->
                    Log.e("FarmViewModel", "Error loading farms", e)
                    _uiState.value = FarmUiState.Error(e.message ?: "Unknown error occurred")
                }
            )
        }
    }

    fun addFarm(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            repository.create(name).fold(
                onSuccess = { created ->
                    onCreated(created.ID)
                    loadFarms()
                },
                onFailure = { e -> Log.e("FarmViewModel", "Error creating farm", e) }
            )
        }
    }

    fun updateFarm(id: Long, name: String) {
        viewModelScope.launch {
            repository.update(id, name).fold(
                onSuccess = { loadFarms() },
                onFailure = { e -> Log.e("FarmViewModel", "Error updating farm", e) }
            )
        }
    }

    fun deleteFarm(id: Long) {
        viewModelScope.launch {
            repository.delete(id).fold(
                onSuccess = { loadFarms() },
                onFailure = { e -> Log.e("FarmViewModel", "Error deleting farm", e) }
            )
        }
    }
}
