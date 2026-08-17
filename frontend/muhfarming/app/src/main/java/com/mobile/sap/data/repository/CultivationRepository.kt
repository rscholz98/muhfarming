package com.mobile.sap.data.repository

import com.mobile.sap.data.api.ApiService
import com.mobile.sap.data.api.RetrofitClient
import com.mobile.sap.data.api.dto.CultivationDto
import com.mobile.sap.data.api.dto.CultivationRequest
import com.mobile.sap.data.api.dto.CultivationGuidelineDto
import com.mobile.sap.data.api.dto.CultivationGuidelineRequest
import com.mobile.sap.data.api.dto.CultivationRiskDto
import com.mobile.sap.data.api.dto.CultivationRiskRequest
import com.mobile.sap.data.api.dto.FertilizerDto
import com.mobile.sap.data.api.dto.HazardDto
import com.mobile.sap.data.api.dto.HazardRequest
import com.mobile.sap.data.event.DataChange
import com.mobile.sap.data.event.DataEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A cultivation joined with its guidelines and risks, ready for display.
 * Fertilizer / hazard names are resolved client-side from their own lists.
 */
data class CultivationGuide(
    val cultivation: CultivationDto,
    val guidelines: List<GuidelineView>,
    val risks: List<RiskView>
)

data class GuidelineView(
    val guideline: CultivationGuidelineDto,
    val fertilizerName: String?
)

data class RiskView(
    val risk: CultivationRiskDto,
    val hazard: HazardDto?
)

/**
 * Cultivation guide data backed by the muhfarming backend. Reads combine
 * `/cultivations`, `/cultivation-guidelines`, `/cultivation-risks`,
 * `/hazards`, and `/fertilizers`, joining them by id client-side. Writes to
 * guidelines / risks / hazards are admin-only on the backend (they 403 for
 * farmers) and are only invoked from admin-gated UI. The JWT is attached
 * automatically by the auth interceptor in [RetrofitClient].
 *
 * Note: `/cultivations` is scoped for farmers to the cultivations they grow
 * (via their crops), so a farmer with no crops sees an empty guide; admins see
 * all cultivations.
 */
class CultivationRepository(
    private val api: ApiService = RetrofitClient.apiService
) {

    /** Load and join everything needed to render the cultivation guide. */
    suspend fun loadGuides(): Result<List<CultivationGuide>> = withContext(Dispatchers.IO) {
        try {
            val cultivationsResp = api.getCultivations()
            if (!cultivationsResp.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to load cultivations (${cultivationsResp.code()})")
                )
            }
            val cultivations = cultivationsResp.body().orEmpty()
            val guidelines = api.getCultivationGuidelines().body().orEmpty()
            val risks = api.getCultivationRisks().body().orEmpty()
            val hazardsById = api.getHazards().body().orEmpty().associateBy { it.ID }
            val fertilizersById = api.getFertilizers().body().orEmpty().associateBy { it.ID }

            val guides = cultivations.map { c ->
                CultivationGuide(
                    cultivation = c,
                    guidelines = guidelines
                        .filter { it.cultivationId == c.ID }
                        .map { g ->
                            GuidelineView(
                                guideline = g,
                                fertilizerName = g.fertilizer?.name
                                    ?: g.fertilizerId?.let { fertilizersById[it]?.name }
                            )
                        },
                    risks = risks
                        .filter { it.cultivationId == c.ID }
                        .map { r ->
                            RiskView(
                                risk = r,
                                hazard = r.hazard ?: hazardsById[r.hazardId]
                            )
                        }
                )
            }
            Result.success(guides)
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading the cultivation guide."))
        }
    }

    /** Plain cultivation list (used by admin dialogs and the alerts screen). */
    suspend fun listCultivations(): Result<List<CultivationDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getCultivations()
            if (resp.isSuccessful) Result.success(resp.body().orEmpty())
            else Result.failure(Exception("Failed to load cultivations (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading cultivations."))
        }
    }

    suspend fun listHazards(): Result<List<HazardDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getHazards()
            if (resp.isSuccessful) Result.success(resp.body().orEmpty())
            else Result.failure(Exception("Failed to load hazards (${resp.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error while loading hazards."))
        }
    }

    // ---- Admin-only writes (backend 403s for non-admins) ----

    suspend fun createCultivation(name: String, estTimeToHarvestWeeks: Int): Result<CultivationDto> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.createCultivation(
                    CultivationRequest(name = name, estTimeToHarvestWeeks = estTimeToHarvestWeeks)
                )
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    DataEvents.emit(DataChange.Cultivation)
                    Result.success(body)
                } else Result.failure(Exception("Failed to create cultivation (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while creating cultivation."))
            }
        }

    suspend fun createHazard(name: String, description: String): Result<HazardDto> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.createHazard(HazardRequest(name = name, description = description))
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    DataEvents.emit(DataChange.Hazard)
                    Result.success(body)
                } else Result.failure(Exception("Failed to create hazard (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while creating hazard."))
            }
        }

    suspend fun createGuideline(req: CultivationGuidelineRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.createCultivationGuideline(req)
                if (resp.isSuccessful) {
                    DataEvents.emit(DataChange.Guideline)
                    Result.success(Unit)
                } else Result.failure(Exception("Failed to create guideline (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while creating guideline."))
            }
        }

    suspend fun createRisk(req: CultivationRiskRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.createCultivationRisk(req)
                if (resp.isSuccessful) {
                    DataEvents.emit(DataChange.Risk)
                    Result.success(Unit)
                } else Result.failure(Exception("Failed to create risk (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while creating risk."))
            }
        }

    // ---- Admin-only updates (backend 403s for non-admins) ----

    suspend fun updateCultivation(id: Long, name: String, estTimeToHarvestWeeks: Int): Result<CultivationDto> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.updateCultivation(
                    id, CultivationRequest(name = name, estTimeToHarvestWeeks = estTimeToHarvestWeeks)
                )
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    DataEvents.emit(DataChange.Cultivation)
                    Result.success(body)
                } else Result.failure(Exception("Failed to update cultivation (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while updating cultivation."))
            }
        }

    suspend fun updateHazard(id: Long, name: String, description: String): Result<HazardDto> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.updateHazard(id, HazardRequest(name = name, description = description))
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    DataEvents.emit(DataChange.Hazard)
                    Result.success(body)
                } else Result.failure(Exception("Failed to update hazard (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while updating hazard."))
            }
        }

    suspend fun updateGuideline(id: Long, req: CultivationGuidelineRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.updateCultivationGuideline(id, req)
                if (resp.isSuccessful) {
                    DataEvents.emit(DataChange.Guideline)
                    Result.success(Unit)
                } else Result.failure(Exception("Failed to update guideline (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while updating guideline."))
            }
        }

    suspend fun updateRisk(id: Long, req: CultivationRiskRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.updateCultivationRisk(id, req)
                if (resp.isSuccessful) {
                    DataEvents.emit(DataChange.Risk)
                    Result.success(Unit)
                } else Result.failure(Exception("Failed to update risk (${resp.code()})"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error while updating risk."))
            }
        }
}
