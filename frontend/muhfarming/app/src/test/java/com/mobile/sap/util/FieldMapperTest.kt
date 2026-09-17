package com.mobile.sap.util

import com.mobile.sap.data.api.dto.FieldCoordinateDto
import com.mobile.sap.data.api.dto.FieldDto
import com.mobile.sap.data.api.dto.RegionDto
import com.mobile.sap.data.model.Coordinate
import com.mobile.sap.data.model.Cultivation
import com.mobile.sap.data.model.Field
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-logic tests for the DTO <-> UI-model translation. No network involved.
 * These guard the trickiest part of the integration: grouping the backend's
 * separate coordinate rows back into a polygon in the right order.
 */
class FieldMapperTest {

    @Test
    fun `toUiField groups only this field's coordinates, ordered by sequence`() {
        val field = FieldDto(ID = 4, name = "Coffee", fieldNotes = "notes", farmId = 4, regionId = 1)
        val coords = listOf(
            // Intentionally out of order + a coordinate for a different field.
            FieldCoordinateDto(ID = 3, latitude = 3.88, longitude = 11.51, sequenceOrder = 1, fieldId = 4),
            FieldCoordinateDto(ID = 9, latitude = 9.99, longitude = 9.99, sequenceOrder = 0, fieldId = 7),
            FieldCoordinateDto(ID = 2, latitude = 3.87, longitude = 11.51, sequenceOrder = 0, fieldId = 4),
            FieldCoordinateDto(ID = 4, latitude = 3.89, longitude = 11.51, sequenceOrder = 2, fieldId = 4),
        )

        val ui = FieldMapper.toUiField(field, coords)

        assertEquals("4", ui.id)
        assertEquals(
            listOf(
                Coordinate(3.87, 11.51),
                Coordinate(3.88, 11.51),
                Coordinate(3.89, 11.51),
            ),
            ui.coordinates
        )
        assertEquals("Coffee", ui.cultivation?.cropType)
        assertEquals("notes", ui.cultivationGuideline)
    }

    @Test
    fun `toUiField prefers nested region name, falls back to regionId`() {
        val withName = FieldDto(ID = 1, name = "F", farmId = 1, regionId = 5, region = RegionDto(ID = 5, name = "Centre"))
        assertEquals("Centre", FieldMapper.toUiField(withName, emptyList()).region)

        val withoutName = FieldDto(ID = 2, name = "F", farmId = 1, regionId = 5, region = RegionDto(ID = 0, name = ""))
        assertEquals("5", FieldMapper.toUiField(withoutName, emptyList()).region)

        val nullRegion = FieldDto(ID = 3, name = "F", farmId = 1, regionId = 5, region = null)
        assertEquals("5", FieldMapper.toUiField(nullRegion, emptyList()).region)
    }

    @Test
    fun `toUiField uses a default crop name when the backend name is blank`() {
        val ui = FieldMapper.toUiField(FieldDto(ID = 1, name = "", farmId = 1, regionId = 1), emptyList())
        assertEquals("Field", ui.cultivation?.cropType)
    }

    @Test
    fun `blank field notes map to null guideline`() {
        val ui = FieldMapper.toUiField(FieldDto(ID = 1, name = "F", fieldNotes = "", farmId = 1, regionId = 1), emptyList())
        assertNull(ui.cultivationGuideline)
    }

    @Test
    fun `toFieldRequest maps crop type to name, guideline to notes, numeric region`() {
        val field = uiField(region = "42", crop = "Maize", guideline = "rotate")
        val req = FieldMapper.toFieldRequest(field, farmId = 7)

        assertEquals("Maize", req.name)
        assertEquals("rotate", req.fieldNotes)
        assertEquals(7L, req.farmId)
        assertEquals(42L, req.regionId)
    }

    @Test
    fun `toFieldRequest falls back to default region id for non-numeric region`() {
        val req = FieldMapper.toFieldRequest(uiField(region = "Centre"), farmId = 1)
        assertEquals(FieldMapper.DEFAULT_REGION_ID, req.regionId)
    }

    @Test
    fun `toCoordinateRequests assigns sequential order and the field id`() {
        val field = uiField(
            coordinates = listOf(
                Coordinate(1.0, 2.0),
                Coordinate(3.0, 4.0),
                Coordinate(5.0, 6.0),
            )
        )

        val reqs = FieldMapper.toCoordinateRequests(field, fieldId = 4)

        assertEquals(3, reqs.size)
        reqs.forEachIndexed { index, req ->
            assertEquals(index, req.sequenceOrder)
            assertEquals(4L, req.fieldId)
        }
        assertEquals(1.0, reqs[0].latitude, 0.0)
        assertEquals(6.0, reqs[2].longitude, 0.0)
    }

    @Test
    fun `mapper round-trips a created field back to the same coordinates`() {
        val original = uiField(
            crop = "Cocoa",
            coordinates = listOf(Coordinate(4.06, 9.76), Coordinate(4.06, 9.77), Coordinate(4.05, 9.76))
        )
        val createdId = 12L

        // Simulate what the backend would echo back on GET after a create.
        val fieldDto = FieldDto(ID = createdId, name = original.cultivation!!.cropType, farmId = 4, regionId = 1)
        val coordDtos = FieldMapper.toCoordinateRequests(original, createdId).mapIndexed { i, r ->
            FieldCoordinateDto(ID = i.toLong(), latitude = r.latitude, longitude = r.longitude, sequenceOrder = r.sequenceOrder, fieldId = r.fieldId)
        }

        val restored = FieldMapper.toUiField(fieldDto, coordDtos)

        assertEquals(original.coordinates, restored.coordinates)
        assertEquals(original.cultivation?.cropType, restored.cultivation?.cropType)
        assertTrue(restored.id == createdId.toString())
    }

    private fun uiField(
        region: String = "1",
        crop: String = "Coffee",
        guideline: String? = null,
        coordinates: List<Coordinate> = listOf(Coordinate(0.0, 0.0), Coordinate(1.0, 1.0), Coordinate(2.0, 2.0)),
    ) = Field(
        id = "0",
        region = region,
        coordinates = coordinates,
        cultivation = Cultivation(cropType = crop, season = "", status = ""),
        cultivationGuideline = guideline,
    )
}
