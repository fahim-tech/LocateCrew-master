package com.example.locatecrew.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locatecrew.data.model.Group
import com.example.locatecrew.data.model.User
import com.example.locatecrew.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            val querySnapshot = groupRepository.getGroups()
            _groups.value = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Group::class.java)
            }
        }
    }

    fun createGroup(group: Group) {
        viewModelScope.launch {
            groupRepository.createGroup(group)
        }
    }

    private val _groupFlow = MutableSharedFlow<Group?>()
    val groupFlow = _groupFlow.asSharedFlow()

    suspend fun getGroupById(groupId: String): Group? {
        return groupRepository.getGroupById(groupId)
    }

    fun getGroupByIdFlow(groupId: String): Flow<Group?> {
        return groupFlow.filterIsInstance<Group>()
            .filter { it.groupid == groupId }
    }

    fun loadGroupById(groupId: String) {
        viewModelScope.launch {
            val group = fetchGroupFromRepository(groupId)
            _groupFlow.emit(group)
        }
    }

    private suspend fun fetchGroupFromRepository(groupId: String): Group? {
        // Call your repository function to fetch the group data
        return groupRepository.getGroupById(groupId)
    }

    fun joinGroup(groupCode: String, username: String) {
        viewModelScope.launch {
            groupRepository.joinGroup(groupCode, username)
        }
    }

    fun getGroupMembersFlow(groupId: String): Flow<List<User>> {
        return groupRepository.getGroupMembersFlow(groupId)
    }
}