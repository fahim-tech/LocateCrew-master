package com.example.locatecrew.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.locatecrew.data.model.Announcement
import com.example.locatecrew.data.model.Comment
import com.example.locatecrew.data.store.LoggedInUserManager
import com.example.locatecrew.ui.components.AnnouncementCard
import com.example.locatecrew.viewmodel.AnnouncementViewModel
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AnnouncementsScreen(
    groupId: String,
    viewModel: AnnouncementViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val announcements by viewModel.announcements.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_announcement/$groupId") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Announcement")
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "announcements") {
            composable("announcements") {
                LazyColumn {
                    items(announcements) { announcement ->
                        if (announcement.groupId == groupId) {
                            AnnouncementCard(
                                announcement = announcement,
                                onAnnouncementClick = {
                                    navController.navigate("announcement/${announcement.id}")
                                }
                            )
                        }
                    }
                }
            }
            composable("announcement/{announcementId}") { backStackEntry ->
                val announcementId = backStackEntry.arguments?.getString("announcementId") ?: ""
                val announcement = runBlocking { viewModel.getAnnouncementById(announcementId) }
                if (announcement != null) {
                    AnnouncementCommentsScreen(announcement, viewModel)
                } else {
                    println("Invalid announcement ID $announcementId")
                    Text("Invalid announcement ID")
                }
            }
            composable(
                route = "add_announcement/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val nowGroupId = backStackEntry.arguments?.getString("groupId")
                if (nowGroupId != null) {
                    AddAnnouncementScreen(nowGroupId, viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun AddAnnouncementScreen(
    groupId: String,
    viewModel: AnnouncementViewModel = hiltViewModel(),
    navController: NavController
) {
    var announcementTitle by remember { mutableStateOf("") }
    var announcementContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = announcementTitle,
            onValueChange = { announcementTitle = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = announcementContent,
            onValueChange = { announcementContent = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val announcement = LoggedInUserManager.getLoggedInUsername()?.let {
                    Announcement(
                        title = announcementTitle,
                        groupId = groupId,
                        creatorUsername = it, // Replace with the actual user's username
                        content = announcementContent,
                        timestamp = System.currentTimeMillis()
                    )
                }
                if (announcement != null) {
                    viewModel.addAnnouncement(announcement)
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Announcement")
        }
    }
}


fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}