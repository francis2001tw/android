package com.example.essentialcomponents.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DropdownMenuItemExample() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("None") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "More options",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = "Selected: $selectedOption",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        selectedOption = "Edit"
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF6200EE)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = {
                        selectedOption = "Share"
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF03DAC5)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        selectedOption = "Delete"
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        selectedOption = "Settings"
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFFF9800)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Profile") },
                    onClick = {
                        selectedOption = "Profile"
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color(0xFF9C27B0)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                )
            }
        }
    }
}