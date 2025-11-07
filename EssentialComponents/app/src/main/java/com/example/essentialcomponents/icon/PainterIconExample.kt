package com.example.essentialcomponents.icon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.essentialcomponents.R

@Composable
fun PainterIconExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "painterResource Examples",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Using Drawable Resources",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Row 1: Default drawable icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Launcher Icon",
                modifier = Modifier.size(64.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Launcher Icon Tinted",
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 2: Different colors
        Text(
            text = "Different Tint Colors",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Pink Icon",
                tint = Color(0xFFE91E63),
                modifier = Modifier.size(64.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Teal Icon",
                tint = Color(0xFF03DAC5),
                modifier = Modifier.size(64.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Orange Icon",
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row 3: Different sizes
        Text(
            text = "Different Sizes",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Small",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Medium",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Large",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Extra Large",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "painterResource loads icons from drawable resources",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
