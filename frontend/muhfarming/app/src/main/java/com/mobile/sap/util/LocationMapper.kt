package com.mobile.sap.util

data class LocationCoordinates(val latitude: Double, val longitude: Double)

object LocationMapper {
    fun getCoordinates(location: String): LocationCoordinates {
        return when (location) {
            "Yaoundé, Cameroon" -> LocationCoordinates(3.8480, 11.5021)
            "Douala, Cameroon" -> LocationCoordinates(4.0511, 9.7679)
            "Garoua, Cameroon" -> LocationCoordinates(9.3013, 13.3964)
            "Bamenda, Cameroon" -> LocationCoordinates(5.9631, 10.1591)
            else -> LocationCoordinates(6.0, 12.0) // Center of Cameroon
        }
    }
}
