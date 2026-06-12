package com.mobile.sap.data.model

data class CameroonCity(
    val name: String,
    val region: String,
    val latitude: Double,
    val longitude: Double
)

object CameroonCities {
    val cities = listOf(
        CameroonCity("Yaoundé", "Centre", 3.848, 11.502),
        CameroonCity("Douala", "Littoral", 4.051, 9.768),
        CameroonCity("Garoua", "Nord", 9.301, 13.396),
        CameroonCity("Bamenda", "Nord-Ouest", 5.963, 10.159),
        CameroonCity("Bafoussam", "Ouest", 5.477, 10.418),
        CameroonCity("Maroua", "Extrême-Nord", 10.591, 14.316),
        CameroonCity("Ngaoundéré", "Adamaoua", 7.322, 13.584),
        CameroonCity("Bertoua", "Est", 4.577, 13.684),
        CameroonCity("Buea", "Sud-Ouest", 4.160, 9.233),
        CameroonCity("Kumba", "Sud-Ouest", 4.636, 9.446)
    )

    fun getCityByName(name: String): CameroonCity? {
        return cities.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getDefaultCity(): CameroonCity = cities.first() // Yaoundé as default
}
