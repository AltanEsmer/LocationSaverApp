package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtrackerapp.ui.theme.ErrorRed

/**
 * Dialog for saving the current location with a user-provided name.
 * Matches the app's minimalist dialog style: clean spacing, a single
 * input, and a clear primary action. The actual location fetch + save
 * happens in the background after the dialog closes; result feedback is
 * shown as a subtle snackbar on the main screen.
 *
 * @param onDismiss Callback when the dialog is dismissed
 * @param onSave Callback when the user confirms saving with a name
 */
@Composable
fun SaveLocationDialog(
    isSaving: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var locationName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    PremiumAlertDialog(
        onDismiss = onDismiss,
        title = "Save Location",
        content = {
            Column {
                OutlinedTextField(
                    value = locationName,
                    onValueChange = {
                        locationName = it
                        isError = false
                    },
                    placeholder = { Text("Location name") },
                    isError = isError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = premiumTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = "Enter a name for this location",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmText = "Save",
        onConfirm = {
            val name = locationName.trim()
            if (name.isNotEmpty()) {
                onSave(name)
            } else {
                isError = true
            }
        },
        confirmClosesDialog = false,
        dismissText = "Cancel"
    )
}
