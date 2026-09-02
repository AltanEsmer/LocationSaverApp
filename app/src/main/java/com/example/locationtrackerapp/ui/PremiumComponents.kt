package com.example.locationtrackerapp.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.locationtrackerapp.ui.theme.BrandRed700
import com.example.locationtrackerapp.ui.theme.DividerColor
import com.example.locationtrackerapp.ui.theme.SurfaceCard
import com.example.locationtrackerapp.ui.theme.TextPrimary
import com.example.locationtrackerapp.ui.theme.TextSecondary

/**
 * Shared dialog shell used across the app (save, rename, delete, errors,
 * location test) so every dialog has the same clean, premium look:
 * rounded corners, minimal supporting text, a clear primary action in the
 * brand color, and a plain secondary action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumAlertDialog(
    onDismiss: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    confirmColor: Color = BrandRed700,
    confirmEnabled: Boolean = true,
    confirmClosesDialog: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = content,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    onConfirm()
                    if (confirmClosesDialog) onDismiss()
                }
            ) {
                Text(confirmText, color = confirmColor, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextSecondary)
            }
        }
    )
}

/** Consistent, brand-colored outlined text field styling for all dialogs. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun premiumTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandRed700,
    unfocusedBorderColor = DividerColor,
    cursorColor = BrandRed700,
    focusedLabelColor = BrandRed700,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
