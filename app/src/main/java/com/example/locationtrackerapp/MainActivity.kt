package com.example.locationtrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtrackerapp.ui.MainScreenAdvanced
import com.example.locationtrackerapp.ui.PermissionHandler
import com.example.locationtrackerapp.ui.theme.LocationTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            setContent {
                LocationTrackerAppTheme {
                    PermissionHandler {
                        MainScreenAdvanced()
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback UI if there's an error
            setContent {
                LocationTrackerAppTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "App initialization error: ${e.message}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
