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

class FieldViewModel : ViewModel() {

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
            Log.d("FieldViewModel", "Loading all fields...")
            try {
                FieldRepository.getFields(null).collect { fields ->
                    Log.d("FieldViewModel", "Loaded ${fields.size} fields from repository")
                    _uiState.value = FieldUiState.Success(fields)
                }
            } catch (e: Exception) {
                Log.e("FieldViewModel", "Error loading fields", e)
                _uiState.value = FieldUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadFields(region: String? = null) {
        viewModelScope.launch {
            _uiState.value = FieldUiState.Loading
            Log.d("FieldViewModel", "Loading fields for region: $region")
            try {
                FieldRepository.getFields(region).collect { fields ->
                    Log.d("FieldViewModel", "Loaded ${fields.size} fields")
                    _uiState.value = FieldUiState.Success(fields)
                }
            } catch (e: Exception) {
                Log.e("FieldViewModel", "Error loading fields", e)
                _uiState.value = FieldUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadFieldsNearLocation(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _uiState.value = FieldUiState.Loading
            Log.d("FieldViewModel", "Loading fields near ($latitude, $longitude) within ${radiusKm}km")
            try {
                FieldRepository.getFieldsNearLocation(latitude, longitude, radiusKm).collect { fields ->
                    Log.d("FieldViewModel", "Found ${fields.size} fields near location")
                    if (fields.isEmpty()) {
                        Log.w("FieldViewModel", "No fields found nearby, loading all fields instead")
                        loadAllFields()
                    } else {
                        _uiState.value = FieldUiState.Success(fields)
                    }
                }
            } catch (e: Exception) {
                Log.e("FieldViewModel", "Error loading nearby fields", e)
                _uiState.value = FieldUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun selectField(field: Field?) {
        _selectedField.value = field
    }

    fun addField(field: Field) {
        viewModelScope.launch {
            Log.d("FieldViewModel", "Adding new field: ${field.id}")
            try {
                // TODO: Make API call to create field when backend is ready
                // For now, add to local state
                val currentState = _uiState.value
                if (currentState is FieldUiState.Success) {
                    val updatedFields = currentState.fields + field
                    _uiState.value = FieldUiState.Success(updatedFields)
                    Log.d("FieldViewModel", "Field added successfully. Total fields: ${updatedFields.size}")
                }
            } catch (e: Exception) {
                Log.e("FieldViewModel", "Error adding field", e)
            }
        }
    }

    fun updateField(updatedField: Field) {
        viewModelScope.launch {
            Log.d("FieldViewModel", "Updating field: ${updatedField.id}")
            try {
                // TODO: Make API call to update field when backend is ready
                // For now, update in local state
                val currentState = _uiState.value
                if (currentState is FieldUiState.Success) {
                    val updatedFields = currentState.fields.map { field ->
                        if (field.id == updatedField.id) updatedField else field
                    }
                    _uiState.value = FieldUiState.Success(updatedFields)
                    Log.d("FieldViewModel", "Field updated successfully")
                }
            } catch (e: Exception) {
                Log.e("FieldViewModel", "Error updating field", e)
            }
        }
    }

    fun deleteField(fieldId: String) {
        viewModelScope.launch {
            Log.d("FieldViewModel", "Deleting field: $fieldId")
            try {
                // TODO: Make API call to delete field when backend is ready
                // For now, remove from local state
                val currentState = _uiState.value
                if (currentState is FieldUiState.Success) {
                    val updatedFields = currentState.fields.filter { it.id != fieldId }
                    _uiState.value = FieldUiState.Success(updatedFields)
                    Log.d("FieldViewModel", "Field deleted successfully. Remaining fields: ${updatedFields.size}")
                }
            } catch (e: Exception) {
                Log.e("FieldViewModel", "Error deleting field", e)
            }
        }
    }
}
