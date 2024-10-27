package com.example.locatecrew.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.locatecrew.data.model.Announcement
import com.example.locatecrew.data.model.Comment
import com.example.locatecrew.ui.components.CommentCard
import com.example.locatecrew.viewmodel.AnnouncementViewModel
import kotlinx.coroutines.runBlocking

@Composable
fun AnnouncementCommentsScreen(
    announcement: Announcement,
    viewModel: AnnouncementViewModel = hiltViewModel()
) {
    val comments by viewModel.comments.collectAsState(initial = emptyList())
    var newComment by remember { mutableStateOf("") }

    Column {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(comments) { comment ->
                if(comment.announcementId == announcement.id) {
                    CommentCard(comment)
                }
            }
        }
        OutlinedTextField(
            value = newComment,
            onValueChange = { newComment = it },
            label = { Text("Add a comment") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                viewModel.addComment(announcement.id, newComment)
                newComment = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comment")
        }
    }
}