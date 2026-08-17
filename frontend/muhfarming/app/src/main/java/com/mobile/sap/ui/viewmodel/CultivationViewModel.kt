package com.mobile.sap.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sap.data.api.dto.CultivationDto
import com.mobile.sap.data.api.dto.CultivationGuidelineRequest
import com.mobile.sap.data.api.dto.CultivationRiskRequest
import com.mobile.sap.data.api.dto.HazardDto
import com.mobile.sap.data.event.DataChange
import com.mobile.sap.data.event.DataEvents
import com.mobile.sap.data.repository.CultivationGuide
import com.mobile.sap.data.repository.CultivationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CultivationUiState {
    object Loading : CultivationUiState()
    data class Success(val guides: List<CultivationGuide>) : CultivationUiState()
    data class Error(val message: String) : CultivationUiState()
}

class CultivationViewModel(
    private val repository: CultivationRepository = CultivationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CultivationUiState>(CultivationUiState.Loading)
    val uiState: StateFlow<CultivationUiState> = _uiState.asStateFlow()

    // Reference lists for the admin authoring dialogs.
    private val _cultivations = MutableStateFlow<List<CultivationDto>>(emptyList())
    val cultivations: StateFlow<List<CultivationDto>> = _cultivations.asStateFlow()

    private val _hazards = MutableStateFlow<List<HazardDto>>(emptyList())
    val hazards: StateFlow<List<HazardDto>> = _hazards.asStateFlow()

    // One-shot user-facing messages (create success / failure), surfaced as
    // snackbars by the screen.
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CultivationUiState.Loading
            repository.loadGuides().fold(
                onSuccess = { guides ->
                    _uiState.value = CultivationUiState.Success(guides)
                    _cultivations.value = guides.map { it.cultivation }
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error loading guides", e)
                    _uiState.value = CultivationUiState.Error(e.message ?: "Unknown error occurred")
                }
            )
            repository.listHazards().onSuccess { _hazards.value = it }
        }
    }

    fun addCultivation(name: String, estTimeToHarvestWeeks: Int, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            repository.createCultivation(name, estTimeToHarvestWeeks).fold(
                onSuccess = { created ->
                    // Make it immediately selectable without waiting for a full reload.
                    _cultivations.value = _cultivations.value + created
                    _messages.tryEmit("Cultivation added")
                    onCreated(created.ID)
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error creating cultivation", e)
                    _messages.tryEmit(e.message ?: "Failed to add cultivation")
                }
            )
        }
    }

    fun addHazard(name: String, description: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            repository.createHazard(name, description).fold(
                onSuccess = { created ->
                    _hazards.value = _hazards.value + created
                    _messages.tryEmit("Hazard added")
                    onCreated(created.ID)
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error creating hazard", e)
                    _messages.tryEmit(e.message ?: "Failed to add hazard")
                }
            )
        }
    }

    fun addGuideline(req: CultivationGuidelineRequest) {
        viewModelScope.launch {
            repository.createGuideline(req).fold(
                onSuccess = {
                    _messages.tryEmit("Guideline added")
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error creating guideline", e)
                    _messages.tryEmit(e.message ?: "Failed to add guideline")
                }
            )
        }
    }

    fun addRisk(req: CultivationRiskRequest) {
        viewModelScope.launch {
            repository.createRisk(req).fold(
                onSuccess = {
                    _messages.tryEmit("Risk added")
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error creating risk", e)
                    _messages.tryEmit(e.message ?: "Failed to add risk")
                }
            )
        }
    }

    fun updateCultivation(id: Long, name: String, estTimeToHarvestWeeks: Int) {
        viewModelScope.launch {
            repository.updateCultivation(id, name, estTimeToHarvestWeeks).fold(
                onSuccess = { updated ->
                    _cultivations.value = _cultivations.value.map { if (it.ID == id) updated else it }
                    _messages.tryEmit("Cultivation updated")
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error updating cultivation", e)
                    _messages.tryEmit(e.message ?: "Failed to update cultivation")
                }
            )
        }
    }

    fun updateHazard(id: Long, name: String, description: String) {
        viewModelScope.launch {
            repository.updateHazard(id, name, description).fold(
                onSuccess = { updated ->
                    _hazards.value = _hazards.value.map { if (it.ID == id) updated else it }
                    _messages.tryEmit("Hazard updated")
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error updating hazard", e)
                    _messages.tryEmit(e.message ?: "Failed to update hazard")
                }
            )
        }
    }

    fun updateGuideline(id: Long, req: CultivationGuidelineRequest) {
        viewModelScope.launch {
            repository.updateGuideline(id, req).fold(
                onSuccess = {
                    _messages.tryEmit("Guideline updated")
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error updating guideline", e)
                    _messages.tryEmit(e.message ?: "Failed to update guideline")
                }
            )
        }
    }

    fun updateRisk(id: Long, req: CultivationRiskRequest) {
        viewModelScope.launch {
            repository.updateRisk(id, req).fold(
                onSuccess = {
                    _messages.tryEmit("Risk updated")
                    load()
                },
                onFailure = { e ->
                    Log.e("CultivationViewModel", "Error updating risk", e)
                    _messages.tryEmit(e.message ?: "Failed to update risk")
                }
            )
        }
    }
}
