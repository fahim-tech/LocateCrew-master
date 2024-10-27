package com.example.locatecrew.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locatecrew.data.model.Group
import com.example.locatecrew.data.model.User
import com.example.locatecrew.ui.components.DashboardScreenContent
import com.example.locatecrew.ui.components.GroupCard
import com.example.locatecrew.viewmodel.GroupViewModel
import com.example.locatecrew.viewmodel.UserViewModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: GroupViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val groups = viewModel.groups.collectAsState(initial = emptyList())
    val currentUser: User? = runBlocking { userViewModel.getCurrentUser() }
    val joinGroupDialogShown = remember { mutableStateOf(false) }
    var groupCodeToJoin by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        viewModel.loadGroups()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { joinGroupDialogShown.value = true },
                    content = { Icon(Icons.Default.Add, contentDescription = "Join Group") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { navController.navigate("create_group") },
                    content = { Icon(Icons.Default.Create, contentDescription = "Create Group") }
                )
            }
        }
    ) { paddingValues ->
        if (joinGroupDialogShown.value) {
            AlertDialog(
                onDismissRequest = { joinGroupDialogShown.value = false },
                title = { Text("Join Group") },
                text = {
                    OutlinedTextField(
                        value = groupCodeToJoin,
                        onValueChange = { groupCodeToJoin = it },
                        label = { Text("Group Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.joinGroup(groupCodeToJoin, currentUser?.username ?: "")
                            joinGroupDialogShown.value = false
                        }
                    ) {
                        Text("Join")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { joinGroupDialogShown.value = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        DashboardScreenContent(
            groups = groups.value.filter { group ->
                group.memberUsernames.contains(currentUser?.username ?: "")
            },
            onGroupClick = { groupId ->
                navController.navigate("group/$groupId") {
                    popUpTo("dashboard") { inclusive = false }
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}