package com.example.locationtrackerapp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Intent
import com.example.locationtrackerapp.ui.theme.*
import com.example.locationtrackerapp.ui.theme.CafeGold
import com.example.locationtrackerapp.ui.theme.CafeOrange
import com.example.locationtrackerapp.ui.theme.LocationBlue
import com.example.locationtrackerapp.ui.theme.RouteRed
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
    var showSearchDialog by remember { mutableStateOf(false) }
    var showAddOrderDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showLocationTestDialog by remember { mutableStateOf(false) }
    var showDataManagementDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    val tabs = listOf(
        TabItem("📍 Konumlar", Icons.Default.LocationOn, "Delivery locations"),
        TabItem("📋 Siparişler", Icons.Default.List, "Order management"),
        TabItem("👤 Müşteriler", Icons.Default.Person, "Customer database"),
        TabItem("🗺️ Rota", Icons.Default.Star, "Route planning")
    )
    
    // Force rebuild trigger - beautiful UI with emojis
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "☕",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Uriel Cafe",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Delivery Manager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showLocationTestDialog = true }) {
                        Text("📍", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Text("🔍", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Text("⚙️", style = MaterialTheme.typography.titleMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "scale"
                    )
                    
                    NavigationBarItem(
                        icon = { 
                            Box(
                                modifier = Modifier.scale(scale),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title.substring(0, 2), // Get emoji
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        label = { 
                            Text(
                                tab.title.substring(3), // Remove emoji from label
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            val fabScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "fabScale"
            )
            
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { 
                        showSaveDialog = true
                    },
                    modifier = Modifier.scale(fabScale),
                    containerColor = CafeGold,
                    contentColor = Color.White
                ) {
                    Text("📍", style = MaterialTheme.typography.titleLarge)
                }
                1 -> FloatingActionButton(
                    onClick = { 
                        android.util.Log.d("MainScreen", "Order FAB clicked, setting showAddOrderDialog = true")
                        showAddOrderDialog = true
                    },
                    modifier = Modifier.scale(fabScale),
                    containerColor = CafeOrange,
                    contentColor = Color.White
                ) {
                    Text("📋", style = MaterialTheme.typography.titleLarge)
                }
                2 -> FloatingActionButton(
                    onClick = { 
                        android.util.Log.d("MainScreen", "Customer FAB clicked, setting showAddCustomerDialog = true")
                        showAddCustomerDialog = true
                    },
                    modifier = Modifier.scale(fabScale),
                    containerColor = LocationBlue,
                    contentColor = Color.White
                ) {
                    Text("👤", style = MaterialTheme.typography.titleLarge)
                }
                3 -> FloatingActionButton(
                    onClick = { 
                        // Route planning doesn't need a FAB
                    },
                    modifier = Modifier.scale(fabScale),
                    containerColor = RouteRed,
                    contentColor = Color.White
                ) {
                    Text("🗺️", style = MaterialTheme.typography.titleLarge)
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
                1 -> OrderManagementScreen(
                    showAddDialog = showAddOrderDialog, 
                    onDialogDismiss = { 
                        showAddOrderDialog = false
                    }
                )
                2 -> CustomerManagementScreen(
                    showAddDialog = showAddCustomerDialog, 
                    onDialogDismiss = { 
                        showAddCustomerDialog = false
                    }
                )
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
    
    // Show search dialog
    if (showSearchDialog) {
        SearchDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { query ->
                // Handle search based on current tab
                when (selectedTab) {
                    0 -> viewModel.searchLocations(query)
                    1 -> { /* Order search handled in OrderManagementScreen */ }
                    2 -> { /* Customer search handled in CustomerManagementScreen */ }
                }
                showSearchDialog = false
            }
        )
    }
    
    
    // Show location test dialog
    if (showLocationTestDialog) {
        LocationTestDialog(
            viewModel = viewModel,
            onDismiss = { showLocationTestDialog = false }
        )
    }
    
    // Show about dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
    
    // Show data management dialog
    if (showDataManagementDialog) {
        DataManagementDialog(
            onDismiss = { showDataManagementDialog = false },
            viewModel = viewModel
        )
    }
    
    // Show settings dialog
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onDataManagementClick = { showDataManagementDialog = true },
            onAboutClick = { showAboutDialog = true },
            onLocationTestClick = { showLocationTestDialog = true }
        )
    }
}

/**
 * Tab item data class with enhanced properties.
 */
data class TabItem(
    val title: String,
    val icon: ImageVector,
    val description: String = ""
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
            // Beautiful empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Gradient background for empty state
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "📍",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No delivery locations yet",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Start building your delivery network by saving customer locations",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CafeGold.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💡 Quick Start Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CafeGold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Tap the 📍 button to save current location\n• Add customer addresses for easy routing\n• Use GPS coordinates for precise deliveries",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
    onDismiss: () -> Unit,
    onDataManagementClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLocationTestClick: () -> Unit = {}
) {
    
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
                        onClick = onDataManagementClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Data")
                    }
                    
                    OutlinedButton(
                        onClick = onAboutClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("About")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Testing buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onLocationTestClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test Location")
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
 * Location test dialog for testing and debugging location functionality.
 */
@Composable
fun LocationTestDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var currentLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Location Test")
        },
        text = {
            Column {
                Text("Test your device's location capabilities:")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Getting location...")
                    }
                } else if (currentLocation != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Current Location:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Latitude: ${String.format("%.6f", currentLocation!!.first)}")
                            Text("Longitude: ${String.format("%.6f", currentLocation!!.second)}")
                        }
                    }
                } else if (error != null) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Tap 'Get Location' to test location services")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Instructions:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("1. Move to a different location")
                Text("2. Tap 'Get Location' to see current coordinates")
                Text("3. Save a location with a different name")
                Text("4. Check if coordinates are different")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    isLoading = true
                    error = null
                    // Get current location
                    viewModel.getCurrentLocationForTesting(
                        onSuccess = { lat, lng ->
                            currentLocation = Pair(lat, lng)
                            isLoading = false
                        },
                        onError = { errorMessage ->
                            error = errorMessage
                            isLoading = false
                        }
                    )
                }
            ) {
                Text("Get Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Search dialog for searching across different tabs.
 */
@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Search")
        },
        text = {
            Column {
                Text("Enter your search query:")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search term") },
                    placeholder = { Text("e.g., customer name, location, order number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        onSearch(searchQuery)
                    }
                }
            ) {
                Text("Search")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Data management dialog.
 */
@Composable
fun DataManagementDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel? = null
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
                
                if (viewModel != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Testing Tools:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("• Clear all locations for testing")
                }
                
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
            if (viewModel != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearAllLocations()
                            onDismiss()
                        }
                    ) {
                        Text("Clear All Locations")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        }
    )
}

