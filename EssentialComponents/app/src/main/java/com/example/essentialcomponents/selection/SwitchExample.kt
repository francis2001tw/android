package com.example.essentialcomponents.selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchExample() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var isChecked by remember { mutableStateOf(false) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Wi-Fi")
            Spacer(modifier = Modifier.width(8.dp))
            var wifiEnabled = false
            Switch(
                checked = wifiEnabled,
                onCheckedChange = { wifiEnabled = it },
                thumbContent = {
                    if (wifiEnabled) Icon(Icons.Default.Check, contentDescription = null)
                }
            )
        }
    }
}