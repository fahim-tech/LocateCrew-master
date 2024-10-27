package com.example.locatecrew.data.repository

import com.example.locatecrew.data.firebase.FirebaseRepository
import com.example.locatecrew.data.model.Announcement
import com.example.locatecrew.data.model.Comment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepository @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend fun getAnnouncementsForGroup(groupId: String): List<Announcement> {
        return firebaseRepository.getAnnouncementsForGroup(groupId)
    }

    suspend fun addComment(comment: Comment) {
        firebaseRepository.addComment(comment)
    }

    suspend fun getAnnouncementById(announcementId: String): Announcement? {
        return firebaseRepository.getAnnouncementById(announcementId)
    }

    fun addAnnouncement(announcement: Announcement) {
        firebaseRepository.addAnnouncement(announcement)
    }

    fun getAnnouncements(callback: (List<Announcement>?, Exception?) -> Unit) {
        firebaseRepository.getAnnouncements(callback)
    }

    fun getComments(callback: (List<Comment>?, Exception?) -> Unit) {
        firebaseRepository.getComments(callback)
    }
}