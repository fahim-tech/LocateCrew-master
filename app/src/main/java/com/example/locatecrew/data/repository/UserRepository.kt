package com.example.locatecrew.data.repository

import com.example.locatecrew.data.firebase.FirebaseRepository
import com.example.locatecrew.data.model.LocationData
import com.example.locatecrew.data.model.User
import com.example.locatecrew.data.store.LoggedInUserManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend fun getUsers() = firebaseRepository.getUsers()

    suspend fun createUser(user: User) = firebaseRepository.createUser(user)

    suspend fun getCurrentUser(): User? {
        val username = LoggedInUserManager.getLoggedInUsername()
        if (username != null) {
            return getUserByUsername(username)
        }
        return null
    }

    suspend fun getUsersByUsernames(usernames: List<String>): List<User> {
        return firebaseRepository.getUsersByUsernames(usernames)
    }

    suspend fun getUserByUsername(username: String): User? {
        return firebaseRepository.getUserByUsername(username)
    }

    suspend fun updateUserLocation(username: String, latitude: Double, longitude: Double) {
        firebaseRepository.updateUserLocation(username, latitude, longitude)
    }

    suspend fun getUserLocation(username: String): LocationData? {
        return firebaseRepository.getUserLocation(username)
    }


}