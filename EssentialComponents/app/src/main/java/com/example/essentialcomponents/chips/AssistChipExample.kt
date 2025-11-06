package com.example.essentialcomponents.chips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
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
fun AssistChipExample() {
    var clickedChip by remember { mutableStateOf("None") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Clicked: $clickedChip",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AssistChip(
            onClick = { clickedChip = "Add Event" },
            label = { Text("Add Event") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF6200EE)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AssistChip(
            onClick = { clickedChip = "Set Location" },
            label = { Text("Set Location") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFFE91E63)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AssistChip(
            onClick = { clickedChip = "Schedule" },
            label = { Text("Schedule") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date",
                    tint = Color(0xFF03DAC5)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color(0xFFE0F7FA),
                labelColor = Color.Black,
                leadingIconContentColor = Color(0xFF03DAC5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AssistChip(
            onClick = { clickedChip = "Settings" },
            label = { Text("Settings") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFFFF9800)
                )
            },
            border = BorderStroke(2.dp, Color(0xFFFF9800))
        )
    }
}
