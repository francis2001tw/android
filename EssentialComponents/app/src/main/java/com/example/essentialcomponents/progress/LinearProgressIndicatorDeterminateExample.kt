package com.example.essentialcomponents.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LinearProgressIndicatorDeterminateExample() {
    var progress by remember { mutableFloatStateOf(0.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Download Progress",
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF03DAC5),
            trackColor = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 24.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            if (progress < 1.0f) {
                progress += 0.1f
            }
        }) {
            Text("Increase Progress")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (progress > 0.0f) {
                progress -= 0.1f
            }
        }) {
            Text("Decrease Progress")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            progress = 0.0f
        }) {
            Text("Reset")
        }
    }
}
