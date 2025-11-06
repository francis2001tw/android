package com.example.essentialcomponents.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BadgedBoxExample() {
    var messageCount by remember { mutableIntStateOf(8) }
    var notificationCount by remember { mutableIntStateOf(15) }
    var favoriteCount by remember { mutableIntStateOf(42) }
    var showDotBadge by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BadgedBox Examples",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Row 1: Basic BadgedBox with numbers
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Messages BadgedBox
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = Color(0xFFE91E63)
                    ) {
                        Text(
                            text = messageCount.toString(),
                            color = Color.White
                        )
                    }
                }
            ) {
                IconButton(onClick = { messageCount++ }) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Messages",
                        tint = Color(0xFF03DAC5),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Notifications BadgedBox
            BadgedBox(
                badge = {
                    Badge {
                        Text(text = notificationCount.toString())
                    }
                }
            ) {
                IconButton(onClick = { notificationCount++ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF6200EE),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Favorites BadgedBox
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = Color(0xFFFF9800)
                    ) {
                        Text(
                            text = favoriteCount.toString(),
                            color = Color.White
                        )
                    }
                }
            ) {
                IconButton(onClick = { favoriteCount++ }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Row 2: BadgedBox with dot badge and without badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Dot Badge (no content)
            BadgedBox(
                badge = {
                    if (showDotBadge) {
                        Badge(
                            containerColor = Color(0xFF4CAF50)
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Profile icon without badge
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Messages: $messageCount | Notifications: $notificationCount | Favorites: $favoriteCount",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Button(onClick = {
                messageCount = 0
                notificationCount = 0
                favoriteCount = 0
            }) {
                Text("Clear All")
            }

            Button(onClick = {
                messageCount++
                notificationCount++
                favoriteCount++
            }) {
                Text("Add to All")
            }
        }

        Button(onClick = { showDotBadge = !showDotBadge }) {
            Text(if (showDotBadge) "Hide Dot Badge" else "Show Dot Badge")
        }
    }
}