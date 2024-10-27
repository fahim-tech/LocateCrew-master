package com.example.locatecrew.data.model

data class Group(
    val groupid: String = "",
    val name: String = "",
    val description: String = "",
    val createdat: Long = 0L,
    val creatorid: String = "",
    val maxDistance: Double = 0.0,
    val memberUsernames: List<String> = emptyList()
)