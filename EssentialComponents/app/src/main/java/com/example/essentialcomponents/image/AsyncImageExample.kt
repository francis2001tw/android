package com.example.essentialcomponents.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage

@Composable
fun AsyncImageExample() {
    val imageUrl = "https://q4.itc.cn/q_70/images03/20240426/712a689a931542b385cf0b9a84a35c67.jpeg"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AsyncImage Examples",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Section 1: Basic AsyncImage
        Text(
            text = "Basic AsyncImage from URL",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AsyncImage(
            model = imageUrl,
            contentDescription = "Cat from web",
            modifier = Modifier
                .width(250.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .padding(bottom = 24.dp)
        )

        // Section 2: AsyncImage with ContentScale
        Text(
            text = "Different ContentScale",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Cat Crop",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Text(
                    text = "Crop",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Cat Fit",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                )
                Text(
                    text = "Fit",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section 3: AsyncImage with Shapes
        Text(
            text = "AsyncImage with Shapes",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // Circular Image
            AsyncImage(
                model = imageUrl,
                contentDescription = "Circular Cat",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            // Rounded Rectangle
            AsyncImage(
                model = imageUrl,
                contentDescription = "Rounded Cat",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section 4: SubcomposeAsyncImage with loading indicator
        Text(
            text = "With Loading Indicator",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Cat with loading",
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF6200EE)
                    )
                }
            },
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AsyncImage loads images from URLs using Coil",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
