package com.example.essentialcomponents.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScrollableTabRowExample() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Home",
        "Favorites",
        "Profile",
        "Messages",
        "Notifications",
        "Search",
        "Settings",
        "About",
        "Tools",
        "Account"
    )
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Favorite,
        Icons.Default.Person,
        Icons.Default.Email,
        Icons.Default.Notifications,
        Icons.Default.Search,
        Icons.Default.Settings,
        Icons.Default.Info,
        Icons.Default.Build,
        Icons.Default.AccountBox
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF6200EE),
            contentColor = Color.White,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = Color.White
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = title,
                            tint = Color.White
                        )
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selected Tab: ${tabs[selectedTabIndex]}",
                fontSize = 24.sp
            )
        }
    }
}
