package com.example.locatecrew.viewmodel

import com.example.locatecrew.services.LocationService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locatecrew.data.model.LocationData
import com.example.locatecrew.data.model.User
import com.example.locatecrew.data.repository.UserRepository
import com.example.locatecrew.data.store.LoggedInUserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @SuppressLint("StaticFieldLeak") private val context: Context // Add a Context parameter here
) : ViewModel() {

    private val _loginStatus = MutableStateFlow<LoginStatus>(LoginStatus.Idle)
    val loginStatus: StateFlow<LoginStatus> = _loginStatus

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _loginStatus.value = LoginStatus.Loading
                val user = userRepository.getUserByUsername(username)
                println("User: $user")
                if (user != null && user.password == password) {
                    println("HERE LOGGING IN")
                    LoggedInUserManager.setLoggedInUsername(username)
                    _loginStatus.value = LoginStatus.Success(user)

                } else {
                    println("COULD NOT LOG IN")
                    _loginStatus.value = LoginStatus.Failed("Invalid credentials")
                }
            } catch (e: Exception) {
                println("WTFFFFFFFFFFFFF")
                println(e)
                _loginStatus.value = LoginStatus.Failed("An error occurred: ${e.message}")
            }
        }
    }

    suspend fun createUser(user: User) {
        userRepository.createUser(user)
    }

    suspend fun getCurrentUser(): User? {
        return userRepository.getCurrentUser()
    }

    suspend fun getUsersByUsernames(usernames: List<String>): List<User> = withContext(Dispatchers.IO) {
        userRepository.getUsersByUsernames(usernames)
    }

    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        userRepository.getUserByUsername(username)
    }

    sealed class LoginStatus {
        object Idle : LoginStatus()
        object Loading : LoginStatus()
        data class Success(val user: User) : LoginStatus()
        data class Failed(val message: String) : LoginStatus()
    }

    // Locations
    fun updateUserLocation(username: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            userRepository.updateUserLocation(username, latitude, longitude)
        }
    }

    private val _locationUpdates = MutableSharedFlow<LocationData>()
    val locationUpdates = _locationUpdates.asSharedFlow()

    fun getUsersByUsernamesFlow(usernames: List<String>): Flow<List<User>> {
        return flow {
            emit(userRepository.getUsersByUsernames(usernames))
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
    }

    fun startLocationUpdates() {
        // Start a foreground service to request location updates
        val intent = Intent(context, LocationService::class.java)
        context.startService(intent)
    }

    fun stopLocationUpdates() {
        // Stop the foreground service to stop location updates
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }

    suspend fun getUserLocation(username: String) : LocationData? {
        return userRepository.getUserLocation(username)
    }
}