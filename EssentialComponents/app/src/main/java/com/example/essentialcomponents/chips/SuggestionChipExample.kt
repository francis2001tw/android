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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
fun SuggestionChipExample() {
    var selectedSuggestion by remember { mutableStateOf("None") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quick Suggestions",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            SuggestionChip(
                onClick = { selectedSuggestion = "Nearby Restaurants" },
                label = { Text("Nearby Restaurants") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Restaurant",
                        tint = Color(0xFFFF9800)
                    )
                }
            )

            SuggestionChip(
                onClick = { selectedSuggestion = "Popular Places" },
                label = { Text("Popular Places") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = Color(0xFFFFD700)
                    )
                }
            )

            SuggestionChip(
                onClick = { selectedSuggestion = "Shopping Malls" },
                label = { Text("Shopping Malls") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Shopping",
                        tint = Color(0xFF6200EE)
                    )
                }
            )

            SuggestionChip(
                onClick = { selectedSuggestion = "Current Location" },
                label = { Text("Current Location") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFE91E63)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color(0xFFFCE4EC),
                    labelColor = Color.Black,
                    iconContentColor = Color(0xFFE91E63)
                )
            )

            SuggestionChip(
                onClick = { selectedSuggestion = "Coffee Shops" },
                label = { Text("Coffee Shops") }
            )

            SuggestionChip(
                onClick = { selectedSuggestion = "Parks" },
                label = { Text("Parks") }
            )

            SuggestionChip(
                onClick = { selectedSuggestion = "Gas Stations" },
                label = { Text("Gas Stations") }
            )

            SuggestionChip(
                onClick = { selectedSuggestion = "Hotels" },
                label = { Text("Hotels") },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color(0xFFE0F7FA),
                    labelColor = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Selected: $selectedSuggestion",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}