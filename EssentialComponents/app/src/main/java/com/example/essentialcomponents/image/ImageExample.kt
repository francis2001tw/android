package com.example.essentialcomponents.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.essentialcomponents.R

@Composable
fun ImageExample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Image Examples",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Row 1: Basic Image with painterResource
        Text(
            text = "Basic Image",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Launcher Icon",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 24.dp)
        )

        // Row 2: Image with ColorFilter
        Text(
            text = "Image with ColorFilter",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Purple Image",
                colorFilter = ColorFilter.tint(Color(0xFF6200EE)),
                modifier = Modifier.size(80.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Pink Image",
                colorFilter = ColorFilter.tint(Color(0xFFE91E63)),
                modifier = Modifier.size(80.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Teal Image",
                colorFilter = ColorFilter.tint(Color(0xFF03DAC5)),
                modifier = Modifier.size(80.dp)
            )
        }

        // Row 3: Image with different shapes
        Text(
            text = "Image with Shapes",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Circular Image
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Circular Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD))
            )

            // Rounded Rectangle Image
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Rounded Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFCE4EC))
            )
        }

        // Row 4: Image with ContentScale
        Text(
            text = "ContentScale.Crop",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Cropped Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3E5F5))
                .padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Image displays graphics using painterResource",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}