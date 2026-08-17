package com.mobile.sap.util

import com.mobile.sap.data.api.dto.FieldCoordinateDto
import com.mobile.sap.data.api.dto.FieldCoordinateRequest
import com.mobile.sap.data.api.dto.FieldDto
import com.mobile.sap.data.api.dto.FieldRequest
import com.mobile.sap.data.model.CameroonRegions
import com.mobile.sap.data.model.Coordinate
import com.mobile.sap.data.model.Field

/**
 * Translates between the normalized backend DTOs (Field + FieldCoordinate rows)
 * and the denormalized UI [Field] model that the map renders.
 *
 * The backend only persists a field's `name`, `fieldNotes`, `regionId`, and its
 * coordinates. Season / status / risk are not backend-backed yet, so they are
 * treated as display-only: they round-trip within a session but are not sent to
 * or restored from the server.
 */
object FieldMapper {

    /** Default region used when the UI region string is not a numeric id. */
    const val DEFAULT_REGION_ID: Long = 1L

    /**
     * Build a UI [Field] from a backend field plus all coordinate rows that
     * belong to it. Coordinates are ordered by `sequenceOrder`.
     */
    fun toUiField(dto: FieldDto, coordinates: List<FieldCoordinateDto>): Field {
        val ordered = coordinates
            .filter { it.fieldId == dto.ID }
            .sortedBy { it.sequenceOrder }
            .map { Coordinate(latitude = it.latitude, longitude = it.longitude) }

        // Prefer the nested region name; fall back to the known Cameroon region
        // for that id, then to the numeric id as text so the details card always
        // shows something.
        val regionLabel = dto.region?.name?.takeIf { it.isNotBlank() }
            ?: CameroonRegions.nameForId(dto.regionId)
            ?: dto.regionId.toString()

        return Field(
            id = dto.ID.toString(),
            name = dto.name?.takeIf { it.isNotBlank() } ?: "Field",
            region = regionLabel,
            farmId = dto.farmId,
            coordinates = ordered,
            cultivationGuideline = dto.fieldNotes?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Map a UI [Field] (from the add/edit form) to a backend field request. A
     * field belongs to a farm and a region: `farmId` is chosen in the form,
     * `name`/`fieldNotes` are free text, and the region string (a Cameroon
     * region name from the dropdown) is mapped to its backend id.
     */
    fun toFieldRequest(field: Field, farmId: Long): FieldRequest = FieldRequest(
        name = field.name.takeIf { it.isNotBlank() } ?: "Field",
        fieldNotes = field.cultivationGuideline,
        farmId = farmId,
        regionId = CameroonRegions.idForName(field.region)
            ?: field.region.trim().toLongOrNull()
            ?: DEFAULT_REGION_ID
    )

    /** Build ordered coordinate requests for a newly created field. */
    fun toCoordinateRequests(field: Field, fieldId: Long): List<FieldCoordinateRequest> =
        field.coordinates.mapIndexed { index, coord ->
            FieldCoordinateRequest(
                latitude = coord.latitude,
                longitude = coord.longitude,
                sequenceOrder = index,
                fieldId = fieldId
            )
        }
}
