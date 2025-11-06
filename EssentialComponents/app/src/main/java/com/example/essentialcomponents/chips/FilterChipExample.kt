package com.example.essentialcomponents.chips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipExample() {
    var selectedSports by remember { mutableStateOf(false) }
    var selectedTech by remember { mutableStateOf(false) }
    var selectedMusic by remember { mutableStateOf(false) }
    var selectedTravel by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Your Interests",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            FilterChip(
                selected = selectedSports,
                onClick = { selectedSports = !selectedSports },
                label = { Text("Sports") },
                leadingIcon = if (selectedSports) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                } else {
                    null
                }
            )

            FilterChip(
                selected = selectedTech,
                onClick = { selectedTech = !selectedTech },
                label = { Text("Technology") },
                leadingIcon = if (selectedTech) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                } else {
                    null
                }
            )

            FilterChip(
                selected = selectedMusic,
                onClick = { selectedMusic = !selectedMusic },
                label = { Text("Music") },
                leadingIcon = if (selectedMusic) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE91E63),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )

            FilterChip(
                selected = selectedTravel,
                onClick = { selectedTravel = !selectedTravel },
                label = { Text("Travel") },
                leadingIcon = if (selectedTravel) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF03DAC5),
                    selectedLabelColor = Color.Black,
                    selectedLeadingIconColor = Color.Black
                )
            )

            FilterChip(
                selected = selectedFood,
                onClick = { selectedFood = !selectedFood },
                label = { Text("Food") },
                leadingIcon = if (selectedFood) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF9800),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = buildString {
                append("Selected: ")
                val selected = mutableListOf<String>()
                if (selectedSports) selected.add("Sports")
                if (selectedTech) selected.add("Technology")
                if (selectedMusic) selected.add("Music")
                if (selectedTravel) selected.add("Travel")
                if (selectedFood) selected.add("Food")
                append(if (selected.isEmpty()) "None" else selected.joinToString(", "))
            },
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}
