package com.example.locatecrew.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.locatecrew.viewmodel.GroupViewModel
import com.example.locatecrew.viewmodel.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.runBlocking

private const val REQUEST_LOCATION_PERMISSION = 1

@Composable
fun MapViewScreen(
    groupId: String,
    viewModel: GroupViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val permissionGranted = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        permissionGranted.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted.value) {
            userViewModel.startLocationUpdates()
        } else {
            userViewModel.stopLocationUpdates()
        }
    }

    val cameraPositionState = rememberCameraPositionState()
    val members by viewModel.getGroupMembersFlow(groupId).collectAsState(emptyList())

    if (permissionGranted.value) {
        val markerPositions = members.mapNotNull { member ->
            member.location?.let { location ->
                LatLng(location.latitude!!, location.longitude!!)
            }
        }

        LaunchedEffect(markerPositions) {
            if (markerPositions.isNotEmpty()) {
                val bounds = markerPositions.let { positions ->
                    val builder = LatLngBounds.Builder()
                    positions.forEach { builder.include(it) }
                    builder.build()
                }
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngBounds(bounds, 500)
                )
            }
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = true)
        ) {
            members.forEach { member ->
                member.location?.let { location ->
                    val position = LatLng(location.latitude!!, location.longitude!!)
                    Marker(
                        state = MarkerState(position = position),
                        title = member.username
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Location permission is required to view the map.")
        }
    }
}