package com.example.essentialcomponents.icon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Icon Examples",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Row 1: Default icons
        Text(
            text = "Default Icons",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home"
            )

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account"
            )
        }

        // Row 2: Colored icons
        Text(
            text = "Colored Icons",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = Color(0xFFE91E63)
            )

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star",
                tint = Color(0xFFFFD700)
            )

            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = Color(0xFF03DAC5)
            )

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color(0xFF6200EE)
            )
        }

        // Row 3: Different sizes
        Text(
            text = "Different Sizes",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Small",
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(16.dp)
            )

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Medium",
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(24.dp)
            )

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Large",
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(32.dp)
            )

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Extra Large",
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Icons are vector graphics from Material Design",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
