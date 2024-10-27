package com.example.locatecrew.data.model

data class Announcement(
    var id: String = "",
    val title: String = "",
    val groupId: String = "",
    val creatorUsername: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)

data class Comment(
    val announcementId: String = "",
    val creatorUsername: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)