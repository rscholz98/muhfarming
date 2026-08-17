package com.mobile.sap.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sap.data.api.dto.CultivationRiskDto
import com.mobile.sap.data.api.dto.FieldDto
import com.mobile.sap.data.api.dto.IncidentDto
import com.mobile.sap.data.api.dto.IncidentRequest
import com.mobile.sap.data.event.DataChange
import com.mobile.sap.data.event.DataEvents
import com.mobile.sap.data.model.CameroonRegions
import com.mobile.sap.data.repository.AlertRepository
import com.mobile.sap.data.repository.AlertView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AlertUiState {
    object Loading : AlertUiState()
    data class Success(val alerts: List<AlertView>) : AlertUiState()
    data class Error(val message: String) : AlertUiState()
}

class AlertViewModel(
    private val repository: AlertRepository = AlertRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlertUiState>(AlertUiState.Loading)
    val uiState: StateFlow<AlertUiState> = _uiState.asStateFlow()

    // Selected region ids for the local filter. Null until first load so we can
    // seed it with the regions of the user's own fields (the default).
    private val _selectedRegionIds = MutableStateFlow<Set<Long>?>(null)
    val selectedRegionIds: StateFlow<Set<Long>?> = _selectedRegionIds.asStateFlow()

    // Reference lists for the admin create-alert dialog.
    private val _fields = MutableStateFlow<List<FieldDto>>(emptyList())
    val fields: StateFlow<List<FieldDto>> = _fields.asStateFlow()

    private val _incidents = MutableStateFlow<List<IncidentDto>>(emptyList())
    val incidents: StateFlow<List<IncidentDto>> = _incidents.asStateFlow()

    // Cultivation risks back the inline create-incident form (an incident must
    // reference a cultivation-risk).
    private val _cultivationRisks = MutableStateFlow<List<CultivationRiskDto>>(emptyList())
    val cultivationRisks: StateFlow<List<CultivationRiskDto>> = _cultivationRisks.asStateFlow()

    // One-shot user-facing messages (create success / failure), surfaced as
    // snackbars by the screen.
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        load()
        // Live refresh when risks/incidents/alerts change anywhere in the app
        // (e.g. a cultivation-risk authored in the Guide tab).
        viewModelScope.launch {
            DataEvents.events.collect { change ->
                when (change) {
                    DataChange.Risk, DataChange.Incident -> refreshReferenceData()
                    DataChange.Alert -> load()
                    else -> Unit
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AlertUiState.Loading
            repository.load().fold(
                onSuccess = { bundle ->
                    _uiState.value = AlertUiState.Success(bundle.alerts)
                    // Seed the filter with the user's field regions on first load only.
                    if (_selectedRegionIds.value == null) {
                        _selectedRegionIds.value = bundle.userFieldRegionIds
                    }
                },
                onFailure = { e ->
                    Log.e("AlertViewModel", "Error loading alerts", e)
                    _uiState.value = AlertUiState.Error(e.message ?: "Unknown error occurred")
                }
            )
            repository.listFields().onSuccess { _fields.value = it }
            repository.listIncidents().onSuccess { _incidents.value = it }
            repository.listCultivationRisks().onSuccess { _cultivationRisks.value = it }
        }
    }

    /**
     * Refresh only the reference lists (fields, incidents, cultivation risks)
     * without resetting the alert list to Loading. Called when the admin opens
     * the create-alert sheet, so risks/incidents authored elsewhere (e.g. in the
     * Guide tab) show up without a full-screen reload.
     */
    fun refreshReferenceData() {
        viewModelScope.launch {
            repository.listFields().onSuccess { _fields.value = it }
            repository.listIncidents().onSuccess { _incidents.value = it }
            repository.listCultivationRisks().onSuccess { _cultivationRisks.value = it }
        }
    }

    /** Toggle a region in the local filter (purely client-side). */
    fun toggleRegion(regionId: Long) {
        val current = _selectedRegionIds.value ?: emptySet()
        _selectedRegionIds.value =
            if (regionId in current) current - regionId else current + regionId
    }

    /** Select every region (the "All" chip). */
    fun selectAllRegions() {
        _selectedRegionIds.value = CameroonRegions.regions.map { it.id }.toSet()
    }

    /** Clear the region filter (the "All" chip when everything is selected). */
    fun clearRegions() {
        _selectedRegionIds.value = emptySet()
    }

    fun createAlert(fieldId: Long, incidentId: Long) {
        viewModelScope.launch {
            repository.createAlert(fieldId, incidentId).fold(
                onSuccess = {
                    _messages.tryEmit("Alert created")
                    load()
                },
                onFailure = { e ->
                    Log.e("AlertViewModel", "Error creating alert", e)
                    _messages.tryEmit(e.message ?: "Failed to create alert")
                }
            )
        }
    }

    fun updateAlert(id: Long, fieldId: Long, incidentId: Long) {
        viewModelScope.launch {
            repository.updateAlert(id, fieldId, incidentId).fold(
                onSuccess = {
                    _messages.tryEmit("Alert updated")
                    load()
                },
                onFailure = { e ->
                    Log.e("AlertViewModel", "Error updating alert", e)
                    _messages.tryEmit(e.message ?: "Failed to update alert")
                }
            )
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteAlert(id).fold(
                onSuccess = {
                    _messages.tryEmit("Alert removed")
                    load()
                },
                onFailure = { e ->
                    Log.e("AlertViewModel", "Error deleting alert", e)
                    _messages.tryEmit(e.message ?: "Failed to remove alert")
                }
            )
        }
    }

    fun createIncident(req: IncidentRequest, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            repository.createIncident(req).fold(
                onSuccess = { created ->
                    _incidents.value = _incidents.value + created
                    _messages.tryEmit("Incident created")
                    onCreated(created.ID)
                },
                onFailure = { e ->
                    Log.e("AlertViewModel", "Error creating incident", e)
                    _messages.tryEmit(e.message ?: "Failed to create incident")
                }
            )
        }
    }
}
