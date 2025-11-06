package com.example.essentialcomponents.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
fun NavigationRailExample() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "Favorites", "Profile", "Settings")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Favorite,
        Icons.Default.Person,
        Icons.Default.Settings
    )

    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = Color(0xFF6200EE),
            contentColor = Color.White,
            header = {
                FloatingActionButton(
                    onClick = { /* Handle FAB click */ },
                    containerColor = Color(0xFFBB86FC),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            },
            modifier = Modifier.fillMaxHeight()
        ) {
            items.forEachIndexed { index, item ->
                NavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = item,
                            color = Color.White
                        )
                    },
                    selected = selectedItem == index,
                    onClick = { selectedItem = index }
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
                text = "Selected: ${items[selectedItem]}",
                fontSize = 24.sp
            )
        }
    }
}
