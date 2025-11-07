package com.example.essentialcomponents.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalDrawerSheetExample() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Favorites", "Profile", "Messages", "Settings")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Favorite,
        Icons.Default.Person,
        Icons.Default.Email,
        Icons.Default.Settings
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFF5F5F5),
                drawerContentColor = Color.Black
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Navigation Menu",
                    fontSize = 24.sp,
                    color = Color(0xFF6200EE),
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = item,
                                tint = if (selectedItem == index) Color.White else Color(0xFF6200EE)
                            )
                        },
                        label = {
                            Text(
                                text = item,
                                color = if (selectedItem == index) Color.White else Color.Black
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF6200EE),
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = Color(0xFF6200EE),
                            unselectedTextColor = Color.Black
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ModalDrawerSheet Example") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Selected: ${items[selectedItem]}",
                    fontSize = 24.sp
                )
            }
        }
    }
}
