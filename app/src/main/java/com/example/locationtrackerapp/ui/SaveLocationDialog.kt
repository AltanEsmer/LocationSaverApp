package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * Dialog for saving a new location with a user-provided name.
 * 
 * @param onDismiss Callback when the dialog is dismissed
 * @param onSave Callback when the user confirms saving with a name
 */
@Composable
fun SaveLocationDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var locationName by remember { mutableStateOf(TextFieldValue("")) }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Save Delivery Address")
        },
        text = {
            Column {
                Text("Enter a name for this delivery address:")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { 
                        locationName = it
                        isError = false
                    },
                    label = { Text("Address name") },
                    placeholder = { Text("e.g., John's House, Office Building, Apartment 5B") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isError) {
                    Text(
                        text = "Please enter an address name",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val name = locationName.text.trim()
                    if (name.isNotEmpty()) {
                        onSave(name)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
