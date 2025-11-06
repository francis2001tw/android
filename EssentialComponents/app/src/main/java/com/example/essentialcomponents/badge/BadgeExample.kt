package com.example.essentialcomponents.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
fun BadgeExample() {
    var notificationCount by remember { mutableIntStateOf(5) }
    var emailCount by remember { mutableIntStateOf(12) }
    var cartCount by remember { mutableIntStateOf(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Badge Examples",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Badge with number
        BadgedBox(
            badge = {
                Badge {
                    Text(text = notificationCount.toString())
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF6200EE)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Badge with number (Email)
        BadgedBox(
            badge = {
                Badge(
                    containerColor = Color(0xFFE91E63)
                ) {
                    Text(
                        text = emailCount.toString(),
                        color = Color.White
                    )
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = Color(0xFF03DAC5)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Badge without number (dot badge)
        BadgedBox(
            badge = {
                Badge(
                    containerColor = Color(0xFFFF9800)
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Shopping Cart",
                tint = Color(0xFF6200EE)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Badge with number (Shopping Cart)
        BadgedBox(
            badge = {
                Badge(
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Text(
                        text = cartCount.toString(),
                        color = Color.White
                    )
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Shopping Cart",
                tint = Color(0xFF4CAF50)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { notificationCount++ }) {
                Text("Add Notification")
            }

            Button(onClick = {
                if (notificationCount > 0) notificationCount--
            }) {
                Text("Clear One")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { emailCount++ }) {
                Text("Add Email")
            }

            Button(onClick = { cartCount++ }) {
                Text("Add to Cart")
            }
        }
    }
}
