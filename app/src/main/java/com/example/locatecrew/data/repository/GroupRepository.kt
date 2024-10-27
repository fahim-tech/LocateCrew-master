package com.example.locatecrew.data.repository

import com.example.locatecrew.data.firebase.FirebaseRepository
import com.example.locatecrew.data.model.Group
import com.example.locatecrew.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) {
    suspend fun getGroups() = firebaseRepository.getGroups()

    suspend fun createGroup(group: Group): String {
        val groupCode = firebaseRepository.createGroup(group)
        firebaseRepository.joinGroup(groupCode, group.creatorid)
        return groupCode
    }

    suspend fun getGroupById(groupId: String): Group? = firebaseRepository.getGroupById(groupId)

    fun loadGroupById(groupId: String) = firebaseRepository.loadGroupById(groupId)

    fun getGroupByIdFlow(groupId: String): Flow<Group?> = firebaseRepository.getGroupByIdFlow(groupId)

    suspend fun joinGroup(groupCode: String, username: String) = firebaseRepository.joinGroup(groupCode, username)
    fun getGroupMembersFlow(groupId: String): Flow<List<User>> {
        return firebaseRepository.getGroupMembersFlow(groupId)
    }
}