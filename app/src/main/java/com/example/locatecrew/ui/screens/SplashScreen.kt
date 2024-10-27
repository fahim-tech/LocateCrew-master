package com.example.locatecrew.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.locatecrew.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val isUserLoggedIn = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        val currentUser = viewModel.getCurrentUser()
        delay(2000) // Simulate a 2-second delay for splash screen
        coroutineScope.launch {
            if (currentUser != null) {
                isUserLoggedIn.value = true
            } else {
                isUserLoggedIn.value = false
            }
        }
    }

    LaunchedEffect(key1 = isUserLoggedIn.value) {
        if (isUserLoggedIn.value) {
            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}