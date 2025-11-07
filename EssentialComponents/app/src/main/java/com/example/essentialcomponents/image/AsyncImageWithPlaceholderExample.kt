package com.example.essentialcomponents.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.essentialcomponents.R

@Composable
fun AsyncImageWithPlaceholderExample() {
    // Valid image URL
    val validImageUrl = "https://q4.itc.cn/q_70/images03/20240426/712a689a931542b385cf0b9a84a35c67.jpeg"

    // Invalid image URL to demonstrate error handling
    val invalidImageUrl = "https://global.discourse-cdn.com/netlify/original/2X/a/aa7544330d7e7c86f4eb2a6c8570aa8f5b4f1493.jpeg"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AsyncImage with Placeholder & Error",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Section 1: AsyncImage with placeholder
        Text(
            text = "With Placeholder (while loading)",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AsyncImage(
            model = validImageUrl,
            contentDescription = "Cat with placeholder",
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(250.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE3F2FD))
                .padding(bottom = 24.dp)
        )

        // Section 2: AsyncImage with error
        Text(
            text = "With Error Handler (invalid URL)",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AsyncImage(
            model = invalidImageUrl,
            contentDescription = "Failed to load",
            error = painterResource(id = R.drawable.ic_launcher_foreground),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFEBEE))
                .padding(bottom = 24.dp)
        )

        // Section 3: AsyncImage with both placeholder and error
        Text(
            text = "With Both Placeholder & Error",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AsyncImage(
            model = validImageUrl,
            contentDescription = "Cat with both",
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
            error = painterResource(id = R.drawable.ic_launcher_background),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(250.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3E5F5))
                .padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Placeholder shows while loading, Error shows on failure",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
