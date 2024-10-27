package com.example.locatecrew.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locatecrew.data.model.Group
import com.example.locatecrew.data.model.User
import com.example.locatecrew.viewmodel.GroupViewModel
import com.example.locatecrew.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: GroupViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    navController: NavController
) {
    val selectedOption = remember { mutableStateOf(GroupDetailOption.MEMBERS) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val group: Group? by viewModel.getGroupByIdFlow(groupId).collectAsState(initial = null)

    LaunchedEffect(groupId) {
        viewModel.loadGroupById(groupId)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                selectedOption = selectedOption,
                onOptionSelected = { option ->
                    selectedOption.value = option
                    coroutineScope.launch { drawerState.close() }
                }
            )
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(group?.name ?: "") },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedOption.value) {
                        GroupDetailOption.MEMBERS -> GroupMembersScreen(group?.memberUsernames ?: emptyList(), userViewModel)
                        GroupDetailOption.ANNOUNCEMENTS -> AnnouncementsScreen(groupId)
                        GroupDetailOption.ABOUT -> AboutScreen(group)
                        GroupDetailOption.MAP_VIEW -> HandleLocationPermissions(userViewModel, viewModel, groupId)
                    }
                }
            }
        }
    )
}

@Composable
fun DrawerContent(
    selectedOption: MutableState<GroupDetailOption>,
    onOptionSelected: (GroupDetailOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Members") },
            selected = selectedOption.value == GroupDetailOption.MEMBERS,
            onClick = { onOptionSelected(GroupDetailOption.MEMBERS) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("Announcements") },
            selected = selectedOption.value == GroupDetailOption.ANNOUNCEMENTS,
            onClick = { onOptionSelected(GroupDetailOption.ANNOUNCEMENTS) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("About") },
            selected = selectedOption.value == GroupDetailOption.ABOUT,
            onClick = { onOptionSelected(GroupDetailOption.ABOUT) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            label = { Text("Map View") },
            selected = selectedOption.value == GroupDetailOption.MAP_VIEW,
            onClick = { onOptionSelected(GroupDetailOption.MAP_VIEW) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun GroupMembersScreen(
    memberUsernames: List<String>,
    viewModel: UserViewModel = hiltViewModel()
) {
    val members = runBlocking {
        memberUsernames.map { username ->
            viewModel.getUserByUsername(username)
        }
    }

    if (members.isNotEmpty()) {
        LazyColumn {
            items(members) { member ->
                if (member != null) {
                    MemberCard(member)
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun HandleLocationPermissions(
    userViewModel: UserViewModel,
    viewModel: GroupViewModel,
    groupId: String
) {
    val context = LocalContext.current
    val locationPermissionGranted = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        locationPermissionGranted.value = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (locationPermissionGranted.value) {
            userViewModel.startLocationUpdates()
        } else {
            userViewModel.stopLocationUpdates()
        }
    }

    if (locationPermissionGranted.value) {
        MapViewScreen(groupId, viewModel, userViewModel)
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Location permission is required to view the map.")
        }
    }
}


@Composable
fun MemberCard(member: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = member.username,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = member.email ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
@Composable
fun AboutScreen(group: Group?) {
    if (group != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Group Details",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            GroupDetailItem(label = "Group Name:", value = group.name)
            GroupDetailItem(label = "Group Code:", value = group.groupid)
            GroupDetailItem(label = "Description:", value = group.description)
            GroupDetailItem(label = "Max Distance:", value = "${group.maxDistance} km")
            GroupDetailItem(
                label = "Created At:",
                value = formatTimestamp(group.createdat)
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun GroupDetailItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary // Set title text color to primary color
            )
            Spacer(modifier = Modifier.height(4.dp)) // Add spacing between title and value
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}



enum class GroupDetailOption {
    MEMBERS, ANNOUNCEMENTS, ABOUT, MAP_VIEW
}