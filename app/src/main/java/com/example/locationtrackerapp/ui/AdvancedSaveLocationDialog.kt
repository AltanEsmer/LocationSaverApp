package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.locationtrackerapp.data.LocationCategory

/**
 * Advanced dialog for saving locations with categories and additional information.
 * Perfect for delivery service management.
 */
@Composable
fun AdvancedSaveLocationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, category: LocationCategory, notes: String, isFavorite: Boolean) -> Unit
) {
    var locationName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LocationCategory.CUSTOMER) }
    var notes by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Yeni Konum Kaydet")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Location name
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { 
                        locationName = it
                        isError = false
                    },
                    label = { Text("Konum Adı") },
                    placeholder = { Text("Örn: Altan'ın Evi, Ahmet'in Ofisi") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    }
                )
                
                // Category selection
                Text(
                    text = "Kategori:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Column {
                    LocationCategory.values().forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getCategoryDisplayName(category),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notlar (Opsiyonel)") },
                    placeholder = { Text("Özel notlar, talimatlar...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                
                // Favorite checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Favorilere ekle",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (isError) {
                    Text(
                        text = "Lütfen konum adı girin",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = locationName.trim()
                    if (name.isNotEmpty()) {
                        onSave(name, selectedCategory, notes.trim(), isFavorite)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

/**
 * Get display name for location category in Turkish.
 */
private fun getCategoryDisplayName(category: LocationCategory): String {
    return when (category) {
        LocationCategory.HOME -> "Ev"
        LocationCategory.WORK -> "İş"
        LocationCategory.CUSTOMER -> "Müşteri"
        LocationCategory.RESTAURANT -> "Restoran"
        LocationCategory.CAFE -> "Kafe"
        LocationCategory.HOSPITAL -> "Hastane"
        LocationCategory.SCHOOL -> "Okul"
        LocationCategory.OTHER -> "Diğer"
    }
}
