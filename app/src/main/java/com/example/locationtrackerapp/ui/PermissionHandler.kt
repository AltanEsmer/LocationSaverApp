package com.example.locationtrackerapp.ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.locationtrackerapp.ui.theme.BackgroundWhite
import com.example.locationtrackerapp.ui.theme.BrandRed700
import com.example.locationtrackerapp.ui.theme.BrandRedTint
import com.example.locationtrackerapp.ui.theme.TextPrimary
import com.example.locationtrackerapp.ui.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult

/**
 * Composable that handles location permission requests.
 * Shows appropriate UI based on permission state.
 *
 * @param content The content to show when permissions are granted
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Check if Google Play Services is available
    val isGooglePlayServicesAvailable = remember {
        try {
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    when {
        !isGooglePlayServicesAvailable -> {
            InfoScreen(
                title = "Google Play Services Required",
                message = "This app needs Google Play Services to work. Please install or update it from the Play Store."
            )
        }
        locationPermissionState.status.isGranted -> {
            content()
        }
        locationPermissionState.status.shouldShowRationale -> {
            InfoScreen(
                title = "Permission Needed",
                message = "Location permission is required to save and test locations.",
                actionText = "Try Again",
                onAction = { locationPermissionState.launchPermissionRequest() }
            )
        }
        else -> {
            InfoScreen(
                title = "Location Permission",
                message = "This app needs your location to save places. Your data stays on this device.",
                actionText = "Grant Permission",
                onAction = { locationPermissionState.launchPermissionRequest() }
            )
        }
    }
}

/**
 * A single, minimalist full-screen message used for permission and
 * availability states, matching the app's premium visual language.
 */
@Composable
private fun InfoScreen(
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = BrandRedTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BrandRed700,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandRed700,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(actionText)
                }
            }
        }
    }
}
