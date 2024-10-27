package com.example.locatecrew.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locatecrew.data.model.Announcement
import com.example.locatecrew.data.model.Comment
import com.example.locatecrew.data.repository.AnnouncementRepository
import com.example.locatecrew.data.store.LoggedInUserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementViewModel @Inject constructor(
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    init {
        viewModelScope.launch {
            // Fetch initial announcements
            announcementRepository.getAnnouncements { announcements, _ ->
                if (announcements != null) {
                    _announcements.value = announcements
                }
            }
            announcementRepository.getComments { comments, _ ->
                if (comments != null) {
                    _comments.value = comments
                }
            }
        }
    }


    fun addAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            announcementRepository.addAnnouncement(announcement)
            // Fetch updated announcements after adding a new one
            announcementRepository.getAnnouncements { announcements, _ ->
                if (announcements != null) {
                    _announcements.value = announcements
                }
            }
        }
    }

    suspend fun getAnnouncementsForGroup(groupId: String): List<Announcement> {
        return announcementRepository.getAnnouncementsForGroup(groupId)
    }

    suspend fun getAnnouncementById(announcementId: String): Announcement? {
        return announcementRepository.getAnnouncementById(announcementId)
    }

    fun addComment(announcementId: String, commentContent: String) {
        viewModelScope.launch {
            val comment = LoggedInUserManager.getLoggedInUsername()?.let {
                Comment(
                    content = commentContent,
                    announcementId = announcementId,
                    creatorUsername = it,
                    timestamp = System.currentTimeMillis()
                )
            }
            if (comment != null) {
                announcementRepository.addComment(comment)
                announcementRepository.getComments { comments, _ ->
                    if (comments != null) {
                        _comments.value = comments
                    }
                }
            }
        }
    }
}