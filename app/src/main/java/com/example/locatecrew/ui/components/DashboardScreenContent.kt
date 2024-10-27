package com.example.locatecrew.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.locatecrew.data.model.Group

@Composable
fun DashboardScreenContent(
    groups: List<Group>, // Update the type here
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(groups) { group ->
            GroupCard(
                group = group,
                onClick = { onGroupClick(group.groupid) }
            )
        }
    }
}