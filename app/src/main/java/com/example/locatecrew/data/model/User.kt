package com.example.locatecrew.data.model

data class User(
    val username: String = "",
    val password: String = "",
    val phone: String? = "",
    val photoPath: String? = null,
    val email: String? = "",
    val location: LocationData? = null,
    val lastUpdatedTimestamp: Long? = null
)