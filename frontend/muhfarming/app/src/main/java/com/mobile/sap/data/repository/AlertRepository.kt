package com.mobile.sap.data.repository

import com.mobile.sap.data.api.ApiService
import com.mobile.sap.data.api.RetrofitClient
import com.mobile.sap.data.api.dto.AlertDto
import com.mobile.sap.data.api.dto.AlertRequest
import com.mobile.sap.data.api.dto.CultivationRiskDto
import com.mobile.sap.data.api.dto.FieldDto
import com.mobile.sap.data.api.dto.IncidentDto
import com.mobile.sap.data.api.dto.IncidentRequest
import com.mobile.sap.data.event.DataChange
import com.mobile.sap.data.event.DataEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * An alert joined with the field it concerns and its incident, plus the
 * resolved region id of that field. Region for filtering comes from the
 * field's `regionId` — the backend does not deep-preload `incident.region` on
 * the alerts list, so we resolve it via the field instead.
 */
data class AlertView(
    val alert: AlertDto,
    val incident: IncidentDto?,
    val fieldName: String?,
    val regionId: Long?
)

/**
 * Bundle returned when loading the alerts screen: the joined alerts plus the
 * distinct region ids of the caller's fields (used as the default filter).
 */
data class AlertsBundle(
    val alerts: List<AlertView>,
    val userFieldRegionIds: Set<Long>
)

/**
 * Alerts data backed by the muhfarming backend. `GET /alerts` is scoped to the
 * caller's fields; region filtering is done client-side. Creating alerts is
 * admin-only on the backend (403 for farmers). The JWT is attached
 * automatically by the auth interceptor in [RetrofitClient].
 */
class AlertRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    suspend fun load(): Result<AlertsBundle> = withContext(Dispatchers.IO) {
        try {
            val alertsResp = api.getAlerts()
            if (!alertsResp.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to load alerts (${alertsResp.code()})")
                )
            }
            val alerts = alertsResp.body().orEmpty()
            val fields = api.getFields().body().orEmpty()
            val incidents = api.getIncidents().body().orEmpty()

            val fieldsById: Map<Long, FieldDto> = fields.associateBy { it.ID }
            val incidentsById: Map<Long, IncidentDto> = incidents.associateBy { it.ID }

            val views = alerts.map { a ->
                val field = a.field?.takeIf { it.ID != 0L } ?: fieldsById[a.fieldId]
                val incident = a.incident?.takeIf { it.ID != 0L } ?: incidentsById[a.incidentId]
                AlertView(
                    alert = a,
                    incident = incident,
                    fieldName = field?.name,
                    regionId = field?.regionId?.takeIf { it > 0 }
                )
            }
            val userRegionIds = fields.mapNotNull { it.regionId.takeIf { r -> r > 0 } }.toSet()
            Result.success(AlertsBundle(views, userRegionIds))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading alerts."))
        }
    }

    /** Admin-only: create an alert linking a field to an incident. */
    suspend fun createAlert(fieldId: Long, incidentId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.createAlert(AlertRequest(fieldId = fieldId, incidentId = incidentId))
                if (resp.isSuccessful) {
                    DataEvents.emit(DataChange.Alert)
                    Result.success(Unit)
                } else Result.failure(Exception("Failed to create alert (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while creating alert."))
            }
        }

    /** Admin-only: update an existing alert's field / incident link. */
    suspend fun updateAlert(id: Long, fieldId: Long, incidentId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.updateAlert(id, AlertRequest(fieldId = fieldId, incidentId = incidentId))
                if (resp.isSuccessful) {
                    DataEvents.emit(DataChange.Alert)
                    Result.success(Unit)
                } else Result.failure(Exception("Failed to update alert (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while updating alert."))
            }
        }

    /** Admin-only: remove an alert. */
    suspend fun deleteAlert(id: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.deleteAlert(id)
                if (resp.isSuccessful) {
                    DataEvents.emit(DataChange.Alert)
                    Result.success(Unit)
                } else Result.failure(Exception("Failed to delete alert (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while deleting alert."))
            }
        }

    /** Fields available for the admin create-alert picker. */
    suspend fun listFields(): Result<List<FieldDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getFields()
            if (resp.isSuccessful) Result.success(resp.body().orEmpty())
            else Result.failure(Exception("Failed to load fields (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading fields."))
        }
    }

    /** Incidents available for the admin create-alert picker. */
    suspend fun listIncidents(): Result<List<IncidentDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getIncidents()
            if (resp.isSuccessful) Result.success(resp.body().orEmpty())
            else Result.failure(Exception("Failed to load incidents (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading incidents."))
        }
    }

    /** Cultivation risks, used to back the inline create-incident form. */
    suspend fun listCultivationRisks(): Result<List<CultivationRiskDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getCultivationRisks()
            if (resp.isSuccessful) Result.success(resp.body().orEmpty())
            else Result.failure(Exception("Failed to load cultivation risks (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading cultivation risks."))
        }
    }

    /** Admin-only: create an incident (attached to a cultivation-risk + region). */
    suspend fun createIncident(req: IncidentRequest): Result<IncidentDto> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.createIncident(req)
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    DataEvents.emit(DataChange.Incident)
                    Result.success(body)
                } else Result.failure(Exception("Failed to create incident (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while creating incident."))
            }
        }
}
