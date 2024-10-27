package com.example.locatecrew.services

interface LocationDataManager {
    fun saveLocationData(userId: String, latitude: Double, longitude: Double): String
    fun getLocationData(referenceId: String): Pair<Double, Double>?
}

class LocationDataManagerImpl : LocationDataManager {
    private val locationData = mutableMapOf<String, Pair<Double, Double>>()

    override fun saveLocationData(userId: String, latitude: Double, longitude: Double): String {
        val referenceId = "${userId}_${System.currentTimeMillis()}"
        locationData[referenceId] = Pair(latitude, longitude)
        return referenceId
    }

    override fun getLocationData(referenceId: String): Pair<Double, Double>? {
        return locationData[referenceId]
    }
}