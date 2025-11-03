package com.example.essentialcomponents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.essentialcomponents.layout.ScaffoldExample
import com.example.essentialcomponents.ui.theme.EssentialComponentsTheme



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EssentialComponentsTheme{
                ScaffoldExample()
            }
        }
    }
}