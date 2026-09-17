package com.mobile.sap.data.model

/**
 * Cameroon's 10 administrative regions. The [id] values match the backend
 * region seed order (see backend `region.EnsureRegions`), so a selected region
 * name maps directly to the `regionId` the API expects.
 */
data class CameroonRegion(
    val id: Long,
    val name: String,
    val geoCode: String
)

object CameroonRegions {
    val regions = listOf(
        CameroonRegion(1, "Adamawa", "AD"),
        CameroonRegion(2, "Centre", "CE"),
        CameroonRegion(3, "East", "ES"),
        CameroonRegion(4, "Extreme North", "EN"),
        CameroonRegion(5, "Littoral", "LT"),
        CameroonRegion(6, "North", "NO"),
        CameroonRegion(7, "Northwest", "NW"),
        CameroonRegion(8, "South", "SU"),
        CameroonRegion(9, "Southwest", "SW"),
        CameroonRegion(10, "West", "OU")
    )

    val names: List<String> = regions.map { it.name }

    /** Backend region id for a region name, or null if not one of the 10. */
    fun idForName(name: String): Long? =
        regions.find { it.name.equals(name.trim(), ignoreCase = true) }?.id

    /** Region name for a backend region id, or null if unknown. */
    fun nameForId(id: Long): String? = regions.find { it.id == id }?.name
}
