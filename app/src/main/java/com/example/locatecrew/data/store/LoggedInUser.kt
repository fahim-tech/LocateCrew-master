package com.example.locatecrew.data.store

object LoggedInUserManager {
    private var loggedInUsername: String? = null

    fun setLoggedInUsername(username: String) {
        loggedInUsername = username
    }

    fun getLoggedInUsername(): String? {
        return loggedInUsername
    }

    fun clearLoggedInUsername() {
        loggedInUsername = null
    }
}