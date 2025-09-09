package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.viewmodel.MainViewModel

/**
 * Advanced main screen with tabs for different features.
 * Perfect for delivery service management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenAdvanced(
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val tabs = listOf(
        TabItem("Konumlar", Icons.Default.LocationOn),
        TabItem("Siparişler", Icons.Default.List),
        TabItem("Müşteriler", Icons.Default.Person),
        TabItem("Rota", Icons.Default.Star)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uriel Cafe Delivery") },
                actions = {
                    IconButton(onClick = { /* Search functionality - TODO: Implement search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { showSaveDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Location")
                }
                1 -> FloatingActionButton(
                    onClick = { /* Add order functionality is in OrderManagementScreen */ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Order")
                }
                2 -> FloatingActionButton(
                    onClick = { /* Add customer functionality is in CustomerManagementScreen */ }
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Add Customer")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> LocationsTab(viewModel = viewModel)
                1 -> OrderManagementScreen()
                2 -> CustomerManagementScreen()
                3 -> RoutePlanningScreen()
            }
        }
    }
    
    // Show save location dialog
    if (showSaveDialog) {
        SaveLocationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.saveCurrentLocation(name)
                showSaveDialog = false
            }
        )
    }
    
    // Show settings dialog
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false }
        )
    }
}

/**
 * Tab item data class.
 */
data class TabItem(
    val title: String,
    val icon: ImageVector
)

/**
 * Locations tab content.
 */
@Composable
fun LocationsTab(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val context = LocalContext.current
    
    // Show error snackbar if there's an error
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Error will be cleared when user interacts with UI
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Success message for last saved location
        uiState.lastSavedLocationId?.let { locationId ->
            val location = savedLocations.find { it.id == locationId }
            location?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Location '${it.name}' saved successfully!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.clearLastSavedLocationId() }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
        
        // Locations list
        if (savedLocations.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No delivery locations saved",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to save delivery addresses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedLocations) { location ->
                    LocationItem(
                        location = location,
                        onLocationClick = { 
                            viewModel.openLocationInMaps(location)?.let { intent ->
                                context.startActivity(intent)
                            }
                        },
                        onDeleteClick = { 
                            viewModel.deleteLocation(location.id)
                        }
                    )
                }
            }
        }
    }
}


/**
 * Enhanced settings dialog for app configuration.
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDataManagementDialog by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Settings")
        },
        text = {
            Column {
                Text("App Configuration", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Location Services",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• GPS location tracking",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Google Maps integration",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // App features
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Features",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Location management",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Order tracking",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Customer management",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Route optimization",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDataManagementDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Data")
                    }
                    
                    OutlinedButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("About")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
    
    // About dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
    
    // Data management dialog
    if (showDataManagementDialog) {
        DataManagementDialog(
            onDismiss = { showDataManagementDialog = false }
        )
    }
}

/**
 * About dialog.
 */
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("About Uriel Cafe Delivery")
        },
        text = {
            Column {
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A comprehensive delivery management app for Uriel Cafe. Manage locations, track orders, maintain customer relationships, and optimize delivery routes.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("• Save and manage delivery locations")
                Text("• Track order status and progress")
                Text("• Maintain customer database")
                Text("• Optimize delivery routes")
                Text("• Google Maps integration")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * Data management dialog.
 */
@Composable
fun DataManagementDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Data Management")
        },
        text = {
            Column {
                Text(
                    text = "Manage your app data:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("• All data is stored locally on your device")
                Text("• No data is sent to external servers")
                Text("• You can export/import data (coming soon)")
                Text("• Clear all data option (coming soon)")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Data includes:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("• Saved delivery locations")
                Text("• Order history and status")
                Text("• Customer information")
                Text("• Route planning data")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

