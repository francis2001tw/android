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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
fun InputChipExample() {
    val tags = remember {
        mutableStateListOf(
            "Android",
            "Kotlin",
            "Jetpack Compose",
            "Material Design",
            "UI/UX"
        )
    }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your Tags",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tags.forEachIndexed { index, tag ->
                InputChip(
                    selected = selectedTag == tag,
                    onClick = {
                        selectedTag = if (selectedTag == tag) null else tag
                    },
                    label = { Text(tag) },
                    avatar = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Tag",
                            tint = if (selectedTag == tag) Color.White else Color(0xFF6200EE)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                tags.removeAt(index)
                                if (selectedTag == tag) {
                                    selectedTag = null
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = if (selectedTag == tag) Color.White else Color.Gray
                            )
                        }
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = Color(0xFF6200EE),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        selectedTrailingIconColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (selectedTag != null) "Selected: $selectedTag" else "No tag selected",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total tags: ${tags.size}",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
