package com.example.essentialcomponents.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ListWithDividerExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            ListItem(
                headlineContent = { Text("Account") },
                supportingContent = { Text("Manage your profile and settings") },
                leadingContent = {
                    Icon(Icons.Default.AccountCircle, contentDescription = null)
                }
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text("Push, Email, and SMS alerts") },
                leadingContent = {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                }
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
    }
}