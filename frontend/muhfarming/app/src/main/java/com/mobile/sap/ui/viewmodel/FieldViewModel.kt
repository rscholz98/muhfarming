package com.mobile.sap.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sap.data.model.Field
import com.mobile.sap.data.repository.FieldRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FieldUiState {
    object Loading : FieldUiState()
    data class Success(val fields: List<Field>) : FieldUiState()
    data class Error(val message: String) : FieldUiState()
}

class FieldViewModel(
    private val repository: FieldRepository = FieldRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FieldUiState>(FieldUiState.Loading)
    val uiState: StateFlow<FieldUiState> = _uiState.asStateFlow()

    private val _selectedField = MutableStateFlow<Field?>(null)
    val selectedField: StateFlow<Field?> = _selectedField.asStateFlow()

    init {
        loadAllFields()
    }

    fun loadAllFields() {
        viewModelScope.launch {
            _uiState.value = FieldUiState.Loading
            repository.getFields(null).fold(
                onSuccess = { fields ->
                    Log.d("FieldViewModel", "Loaded ${fields.size} fields from backend")
                    _uiState.value = FieldUiState.Success(fields)
                },
                onFailure = { e ->
                    Log.e("FieldViewModel", "Error loading fields", e)
                    _uiState.value = FieldUiState.Error(e.message ?: "Unknown error occurred")
                }
            )
        }
    }

    fun loadFields(region: String? = null) {
        viewModelScope.launch {
            _uiState.value = FieldUiState.Loading
            repository.getFields(region).fold(
                onSuccess = { _uiState.value = FieldUiState.Success(it) },
                onFailure = { e ->
                    _uiState.value = FieldUiState.Error(e.message ?: "Unknown error occurred")
                }
            )
        }
    }

    fun loadFieldsNearLocation(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _uiState.value = FieldUiState.Loading
            repository.getFieldsNearLocation(latitude, longitude, radiusKm).fold(
                onSuccess = { fields ->
                    // Fall back to all fields if none are nearby so the map isn't empty.
                    if (fields.isEmpty()) loadAllFields()
                    else _uiState.value = FieldUiState.Success(fields)
                },
                onFailure = { e ->
                    _uiState.value = FieldUiState.Error(e.message ?: "Unknown error occurred")
                }
            )
        }
    }

    fun selectField(field: Field?) {
        _selectedField.value = field
    }

    fun addField(field: Field, farmId: Long) {
        viewModelScope.launch {
            repository.createField(field, farmId).fold(
                onSuccess = {
                    Log.d("FieldViewModel", "Field created; reloading")
                    loadAllFields()
                },
                onFailure = { e -> Log.e("FieldViewModel", "Error creating field", e) }
            )
        }
    }

    fun updateField(updatedField: Field) {
        viewModelScope.launch {
            repository.updateField(updatedField).fold(
                onSuccess = {
                    Log.d("FieldViewModel", "Field updated; reloading")
                    loadAllFields()
                },
                onFailure = { e -> Log.e("FieldViewModel", "Error updating field", e) }
            )
        }
    }

    fun deleteField(fieldId: String) {
        viewModelScope.launch {
            repository.deleteField(fieldId).fold(
                onSuccess = {
                    Log.d("FieldViewModel", "Field deleted; reloading")
                    loadAllFields()
                },
                onFailure = { e -> Log.e("FieldViewModel", "Error deleting field", e) }
            )
        }
    }
}
