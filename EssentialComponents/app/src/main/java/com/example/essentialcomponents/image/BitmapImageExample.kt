package com.example.essentialcomponents.image

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.essentialcomponents.R

@Composable
fun BitmapImageExample() {
    val context = LocalContext.current

    // Load bitmap from drawable resources
    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.cat)
    val imageBitmap = bitmap.asImageBitmap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bitmap Image Examples",
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Row 1: Basic Bitmap Image
        Text(
            text = "Basic Bitmap Image",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Image(
            bitmap = imageBitmap,
            contentDescription = "Cat Image",
            modifier = Modifier
                .width(200.dp)
                .height(150.dp)
                .padding(bottom = 24.dp)
        )

        // Row 2: Bitmap with ContentScale
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
            // ContentScale.Crop
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = imageBitmap,
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

            // ContentScale.Fit
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = imageBitmap,
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

        // Row 3: Bitmap with Shapes
        Text(
            text = "Bitmap with Shapes",
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
                bitmap = imageBitmap,
                contentDescription = "Circular Cat",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            // Rounded Rectangle
            Image(
                bitmap = imageBitmap,
                contentDescription = "Rounded Cat",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bitmap loaded from drawable using asImageBitmap()",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}