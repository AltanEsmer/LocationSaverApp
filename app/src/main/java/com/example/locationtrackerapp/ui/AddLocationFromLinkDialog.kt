package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.locationtrackerapp.ui.theme.TextSecondary

/**
 * Dialog for saving a location from a pasted Google Maps link — including
 * shortened "maps.app.goo.gl" share links, or a plain "lat,lng" pair.
 *
 * The link is resolved to coordinates in the background after the dialog
 * closes (matching [SaveLocationDialog]'s pattern); success/failure is
 * shown as a snackbar on the main screen.
 *
 * @param onDismiss Callback when the dialog is dismissed
 * @param onSave Callback with the optional name and the pasted link/text
 */
@Composable
fun AddLocationFromLinkDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, link: String) -> Unit
) {
    var link by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    PremiumAlertDialog(
        onDismiss = onDismiss,
        title = "Add from Link",
        content = {
            Column {
                Text(
                    text = "Paste a Google Maps link (Share \u2192 Copy link).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = {
                        link = it
                        isError = false
                    },
                    placeholder = { Text("https://maps.app.goo.gl/...") },
                    isError = isError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = premiumTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Location name (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = premiumTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = "Paste a Google Maps link first",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmText = "Save",
        onConfirm = {
            val trimmedLink = link.trim()
            if (trimmedLink.isEmpty()) {
                isError = true
            } else {
                onSave(name.trim(), trimmedLink)
            }
        },
        confirmClosesDialog = false,
        dismissText = "Cancel"
    )
}
