package com.example.locationtrackerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import com.example.locationtrackerapp.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Advanced route planning screen for delivery optimization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlanningScreen(
    viewModel: RouteViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val route by viewModel.route.collectAsState()
    val finalDestination by viewModel.finalDestination.collectAsState()
    val completePath by viewModel.completePath.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val routeAnalytics by viewModel.routeAnalytics.collectAsState()
    val savedRoutes by viewModel.savedRoutes.collectAsState()
    
    var showRouteOptions by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSavedRoutes by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Auto-clear error after 5 seconds
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with beautiful design
        TopAppBar(
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🗺️",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Route Planning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Optimize delivery routes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { showSavedRoutes = true }) {
                    Text("📚", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = { 
                    // Simple test - show a basic dialog first
                    showRouteOptions = true 
                }) {
                    Text("🗺️", style = MaterialTheme.typography.titleMedium)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        // Beautiful Tab Row with emojis
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📋")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Orders")
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛣️")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Route")
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analytics")
                    }
                }
            )
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> OrdersTab(
                pendingOrders = pendingOrders,
                route = route,
                finalDestination = finalDestination,
                currentLocation = currentLocation,
                onToggleRoute = { locationId ->
                    if (route.any { it.id == locationId }) {
                        viewModel.removeFromRoute(locationId)
                    } else {
                        viewModel.addToRoute(locationId)
                    }
                },
                onSetFinalDestination = { locationId -> viewModel.setFinalDestination(locationId) },
                onSetCurrentLocation = { lat, lon, address -> viewModel.setCurrentLocation(lat, lon, address) },
                onGenerateRoute = { viewModel.generateOptimizedRoute() },
                isLoading = uiState.isLoading
            )
            1 -> RouteTab(
                route = route,
                finalDestination = finalDestination,
                completePath = completePath,
                currentLocation = currentLocation,
                routeAnalytics = routeAnalytics,
                onOpenMaps = { 
                    viewModel.openRouteInMaps()?.let { intent ->
                        context.startActivity(intent)
                    }
                },
                onClearRoute = { viewModel.clearRoute() },
                onSaveRoute = { showSaveDialog = true },
                onExportRoute = { showExportDialog = true },
                onSetFinalDestination = { locationId -> viewModel.setFinalDestination(locationId) }
            )
            2 -> AnalyticsTab(
                routeAnalytics = routeAnalytics,
                route = route
            )
        }
        
        // Error Snackbar
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
                    Text("⚠️")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showRouteOptions) {
        AdvancedRouteOptionsDialog(
            onDismiss = { showRouteOptions = false },
            onApply = { maxStops, avoidTolls, avoidHighways, avoidFerries, optimizeFor, strategy, capacity ->
                viewModel.updateAdvancedRouteOptions(maxStops, avoidTolls, avoidHighways, avoidFerries, optimizeFor, strategy, capacity)
                showRouteOptions = false
            }
        )
    }
    
    if (showSaveDialog) {
        SaveRouteDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name -> 
                viewModel.saveRoute(name)
                showSaveDialog = false
            }
        )
    }
    
    if (showExportDialog) {
        ExportRouteDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                val exportData = viewModel.exportRoute(format)
                // Handle export (save to file, share, etc.)
                showExportDialog = false
            }
        )
    }
    
    if (showSavedRoutes) {
        SavedRoutesDialog(
            savedRoutes = savedRoutes,
            onDismiss = { showSavedRoutes = false },
            onLoadRoute = { routeId -> 
                viewModel.loadRoute(routeId)
                showSavedRoutes = false
            },
            onDeleteRoute = { routeId -> viewModel.deleteRoute(routeId) }
        )
    }
}

/**
 * Orders tab for managing pending orders and route selection.
 */
@Composable
fun OrdersTab(
    pendingOrders: List<OrderEntity>,
    route: List<LocationEntity>,
    finalDestination: LocationEntity?,
    currentLocation: LocationEntity?,
    onToggleRoute: (Long) -> Unit,
    onSetFinalDestination: (Long) -> Unit,
    onSetCurrentLocation: (Double, Double, String) -> Unit,
    onGenerateRoute: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Current location setup
        CurrentLocationSetupCard(
            currentLocation = currentLocation,
            onSetCurrentLocation = onSetCurrentLocation
        )
        
        // Generate route button
        if (pendingOrders.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗺️")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Route Planning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${route.size} locations selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (finalDestination != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Final destination: ${finalDestination.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onGenerateRoute,
                        enabled = !isLoading && route.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isLoading) "Optimizing..." else "Generate Optimized Route")
                    }
                }
            }
        }
        
        // Orders list
        if (pendingOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📋", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No pending orders",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Orders with 'Preparing' or 'Ready' status will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingOrders) { order ->
                    val isInRoute = route.any { it.id == order.locationId }
                    val isFinalDestination = finalDestination?.id == order.locationId
                    
                    EnhancedRouteOrderItem(
                        order = order,
                        isInRoute = isInRoute,
                        isFinalDestination = isFinalDestination,
                        onToggleRoute = { 
                            order.locationId?.let { locationId ->
                                onToggleRoute(locationId)
                            }
                        },
                        onSetFinalDestination = {
                            order.locationId?.let { locationId ->
                                onSetFinalDestination(locationId)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Route tab for displaying optimized route and controls.
 */
@Composable
fun RouteTab(
    route: List<LocationEntity>,
    finalDestination: LocationEntity?,
    completePath: List<LocationEntity>,
    currentLocation: LocationEntity?,
    routeAnalytics: RouteAnalytics,
    onOpenMaps: () -> Unit,
    onClearRoute: () -> Unit,
    onSaveRoute: () -> Unit,
    onExportRoute: () -> Unit,
    onSetFinalDestination: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (route.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🗺️", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No route generated",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select orders and generate an optimized route",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Current location card
            if (currentLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Starting Point",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentLocation.address,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        
                        Text(
                            text = "${String.format("%.6f", currentLocation.latitude)}, ${String.format("%.6f", currentLocation.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Final destination card
            if (finalDestination != null) {
                Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎯")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Final Destination",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = finalDestination.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        if (finalDestination.address.isNotEmpty()) {
                            Text(
                                text = finalDestination.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        
                        Text(
                            text = "${String.format("%.6f", finalDestination.latitude)}, ${String.format("%.6f", finalDestination.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Route summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optimized Route",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Route metrics
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            MetricCard(
                                icon = null,
                                title = "Stops",
                                value = "${route.size}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        item {
                            MetricCard(
                                icon = null,
                                title = "Total Path",
                                value = "${completePath.size}",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        item {
                            MetricCard(
                                icon = null,
                                title = "Distance",
                                value = "${String.format("%.1f", routeAnalytics.totalDistance)} km",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        item {
                            MetricCard(
                                icon = null,
                                title = "Time",
                                value = "${routeAnalytics.estimatedTime} min",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                            onClick = onOpenMaps,
                    modifier = Modifier.weight(1f)
                ) {
                            Text("🗺️")
                    Spacer(modifier = Modifier.width(4.dp))
                            Text("Open in Maps")
                }
                
                OutlinedButton(
                            onClick = onSaveRoute,
                            modifier = Modifier.weight(1f)
                ) {
                            Text("⭐")
                    Spacer(modifier = Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportRoute,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📤")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export")
                        }
                        
                        OutlinedButton(
                            onClick = onClearRoute,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🗑️")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear")
                        }
                    }
                }
            }
            
            // Complete path list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(completePath) { location ->
                    val isFinalDestination = location.id == finalDestination?.id
                    val isCurrentLocation = location.id == currentLocation?.id
                    val stopNumber = completePath.indexOf(location) + 1
                    
                    when {
                        isCurrentLocation -> CurrentLocationItem(
                            location = location,
                            stopNumber = stopNumber
                        )
                        isFinalDestination -> FinalDestinationItem(
                            location = location,
                            stopNumber = stopNumber
                        )
                        else -> RouteStopItem(
                            location = location,
                            stopNumber = stopNumber
                        )
                    }
                }
                    }
                }
            }
        }
        
/**
 * Analytics tab for route performance metrics.
 */
@Composable
fun AnalyticsTab(
    routeAnalytics: RouteAnalytics,
    route: List<LocationEntity>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (route.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📊", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No analytics available",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Generate a route to see performance analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
                            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Performance overview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Performance Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricItem(
                                    title = "Total Distance",
                                    value = "${String.format("%.2f", routeAnalytics.totalDistance)} km",
                                    icon = null
                                )
                                MetricItem(
                                    title = "Estimated Time",
                                    value = "${routeAnalytics.estimatedTime} min",
                                    icon = null
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricItem(
                                    title = "Total Stops",
                                    value = "${routeAnalytics.totalStops}",
                                    icon = null
                                )
                                MetricItem(
                                    title = "Efficiency",
                                    value = "${String.format("%.1f", routeAnalytics.efficiency)} km/stop",
                                    icon = null
                                )
                            }
                        }
                    }
                }
                
                item {
                    // Efficiency chart placeholder
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Route Efficiency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Simple bar chart representation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                EfficiencyBar(
                                    label = "Distance",
                                    value = routeAnalytics.totalDistance,
                                    maxValue = 100.0,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                EfficiencyBar(
                                    label = "Time",
                                    value = routeAnalytics.estimatedTime.toDouble(),
                                    maxValue = 120.0,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                EfficiencyBar(
                                    label = "Stops",
                                    value = routeAnalytics.totalStops.toDouble(),
                                    maxValue = 20.0,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Current location setup card.
 */
@Composable
fun CurrentLocationSetupCard(
    currentLocation: LocationEntity?,
    onSetCurrentLocation: (Double, Double, String) -> Unit
) {
    var showLocationDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (currentLocation != null) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📍")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Current Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (currentLocation != null) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (currentLocation != null) {
                Text(
                    text = currentLocation.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${String.format("%.6f", currentLocation.latitude)}, ${String.format("%.6f", currentLocation.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Set your current location to calculate accurate distances",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { showLocationDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (currentLocation != null) "Update Location" else "Set Current Location"
                )
            }
        }
    }
    
    if (showLocationDialog) {
        SetLocationDialog(
            onDismiss = { showLocationDialog = false },
            onLocationSet = { lat, lon, address ->
                onSetCurrentLocation(lat, lon, address)
                showLocationDialog = false
            }
        )
    }
}

/**
 * Dialog for setting current location.
 */
@Composable
fun SetLocationDialog(
    onDismiss: () -> Unit,
    onLocationSet: (Double, Double, String) -> Unit
) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Current Location")
        },
        text = {
            Column {
                Text(
                    text = "Enter your current location coordinates:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    placeholder = { Text("e.g., 37.421998") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    placeholder = { Text("e.g., -122.084000") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address (optional)") },
                    placeholder = { Text("e.g., 1600 Amphitheatre Pkwy, Mountain View, CA") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val lat = latitude.toDouble()
                        val lon = longitude.toDouble()
                        onLocationSet(lat, lon, address)
                    } catch (e: NumberFormatException) {
                        // Handle invalid input
                    }
                },
                enabled = latitude.isNotEmpty() && longitude.isNotEmpty()
            ) {
                Text("Set Location")
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
 * Enhanced route order item with better visual design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRouteOrderItem(
    order: OrderEntity,
    isInRoute: Boolean,
    isFinalDestination: Boolean,
    onToggleRoute: () -> Unit,
    onSetFinalDestination: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleRoute() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFinalDestination -> MaterialTheme.colorScheme.secondaryContainer
                isInRoute -> MaterialTheme.colorScheme.primaryContainer 
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Order status indicator
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = when (order.status) {
                    OrderStatus.PREPARING -> Color(0xFFFF9800)
                    OrderStatus.READY -> Color(0xFF4CAF50)
                    else -> MaterialTheme.colorScheme.outline
                }
            ) {}
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Order details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = order.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isFinalDestination -> MaterialTheme.colorScheme.onSecondaryContainer
                        isInRoute -> MaterialTheme.colorScheme.onPrimaryContainer 
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (order.orderNumber.isNotEmpty()) {
                    Text(
                        text = "Order #${order.orderNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isFinalDestination -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            isInRoute -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Text(
                    text = getStatusText(order.status),
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isFinalDestination -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        isInRoute -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Action buttons
            Row {
                // Final destination button
                IconButton(
                    onClick = onSetFinalDestination
                ) {
                    Text(
                        text = if (isFinalDestination) "🎯" else "🎯",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isFinalDestination) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Route toggle button
            IconButton(
                onClick = onToggleRoute
            ) {
                    Text(
                        text = if (isInRoute) "❌" else "➕",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

/**
 * Current location item with special styling.
 */
@Composable
fun CurrentLocationItem(
    location: LocationEntity,
    stopNumber: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current location indicator
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Location details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                if (location.address.isNotEmpty()) {
                    Text(
                        text = location.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            
            // Current location label
            Surface(
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiary
            ) {
                Text(
                    text = "START",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
    }
}

/**
 * Final destination item with special styling.
 */
@Composable
fun FinalDestinationItem(
    location: LocationEntity,
    stopNumber: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Final destination indicator
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Location details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                if (location.address.isNotEmpty()) {
                    Text(
                        text = location.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            
            // Final destination label
            Surface(
                modifier = Modifier.padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = "FINAL",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

/**
 * Route stop item for displaying route stops.
 */
@Composable
fun RouteStopItem(
    location: LocationEntity,
    stopNumber: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stop number indicator
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stopNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Location details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (location.address.isNotEmpty()) {
                    Text(
                        text = location.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Location category icon
            Text(
                text = when (location.category) {
                    com.example.locationtrackerapp.data.LocationCategory.HOME -> "🏠"
                    com.example.locationtrackerapp.data.LocationCategory.WORK -> "🏢"
                    com.example.locationtrackerapp.data.LocationCategory.CUSTOMER -> "👤"
                    com.example.locationtrackerapp.data.LocationCategory.RESTAURANT -> "🍽️"
                    com.example.locationtrackerapp.data.LocationCategory.CAFE -> "☕"
                    com.example.locationtrackerapp.data.LocationCategory.HOSPITAL -> "🏥"
                    com.example.locationtrackerapp.data.LocationCategory.SCHOOL -> "🏫"
                    else -> "📍"
                },
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

/**
 * Metric card for displaying route statistics.
 */
@Composable
fun MetricCard(
    icon: ImageVector?,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Metric item for analytics display.
 */
@Composable
fun MetricItem(
    title: String,
    value: String,
    icon: ImageVector?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Efficiency bar for analytics visualization.
 */
@Composable
fun EfficiencyBar(
    label: String,
    value: Double,
    maxValue: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((value / maxValue * 100).dp.coerceAtMost(100.dp))
                    .background(color)
                    .align(Alignment.BottomCenter)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("%.1f", value),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Advanced route options dialog with comprehensive settings.
 */
@Composable
fun AdvancedRouteOptionsDialog(
    onDismiss: () -> Unit,
    onApply: (Int, Boolean, Boolean, Boolean, String, OptimizationStrategy, Int) -> Unit
) {
    var maxStops by remember { mutableStateOf(10) }
    var avoidTolls by remember { mutableStateOf(false) }
    var avoidHighways by remember { mutableStateOf(false) }
    var avoidFerries by remember { mutableStateOf(false) }
    var optimizeFor by remember { mutableStateOf("time") }
    var strategy by remember { mutableStateOf(OptimizationStrategy.NEAREST_NEIGHBOR) }
    var vehicleCapacity by remember { mutableStateOf(100) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Advanced Route Options") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                Text("Maximum stops per route:")
                Slider(
                    value = maxStops.toFloat(),
                    onValueChange = { maxStops = it.toInt() },
                        valueRange = 1f..50f,
                        steps = 48
                )
                Text("$maxStops stops")
                }
                
                item {
                    Text("Vehicle capacity (kg):")
                    Slider(
                        value = vehicleCapacity.toFloat(),
                        onValueChange = { vehicleCapacity = it.toInt() },
                        valueRange = 10f..1000f,
                        steps = 98
                    )
                    Text("$vehicleCapacity kg")
                }
                
                item {
                    Text("Avoidances:")
                    Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = avoidTolls,
                        onCheckedChange = { avoidTolls = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Avoid tolls")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = avoidHighways,
                                onCheckedChange = { avoidHighways = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Avoid highways")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = avoidFerries,
                                onCheckedChange = { avoidFerries = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Avoid ferries")
                        }
                    }
                }
                
                item {
                Text("Optimize for:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { optimizeFor = "time" },
                        label = { Text("Time") },
                        selected = optimizeFor == "time"
                    )
                    FilterChip(
                        onClick = { optimizeFor = "distance" },
                        label = { Text("Distance") },
                        selected = optimizeFor == "distance"
                    )
                    }
                }
                
                item {
                    Text("Optimization Strategy:")
                    Column {
                        OptimizationStrategy.values().forEach { optStrategy ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { strategy = optStrategy }
                            ) {
                                RadioButton(
                                    selected = strategy == optStrategy,
                                    onClick = { strategy = optStrategy }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (optStrategy) {
                                        OptimizationStrategy.NEAREST_NEIGHBOR -> "Nearest Neighbor (Fast)"
                                        OptimizationStrategy.GENETIC_ALGORITHM -> "Genetic Algorithm (Best)"
                                        OptimizationStrategy.SIMULATED_ANNEALING -> "Simulated Annealing (Good)"
                                        OptimizationStrategy.TABU_SEARCH -> "Tabu Search (Advanced)"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(maxStops, avoidTolls, avoidHighways, avoidFerries, optimizeFor, strategy, vehicleCapacity)
                }
            ) {
                Text("Apply")
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
 * Save route dialog.
 */
@Composable
fun SaveRouteDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var routeName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Route") },
        text = {
            Column {
                Text("Enter a name for this route:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Name") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(routeName) },
                enabled = routeName.isNotBlank()
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

/**
 * Export route dialog.
 */
@Composable
fun ExportRouteDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Route") },
        text = {
            Column {
                Text("Choose export format:")
                Spacer(modifier = Modifier.height(16.dp))
                
                ExportFormat.values().forEach { format ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExport(format) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (format) {
                                    ExportFormat.GPX -> "🗺️"
                                    ExportFormat.KML -> "🌍"
                                    ExportFormat.CSV -> "📊"
                                    ExportFormat.JSON -> "📄"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (format) {
                                    ExportFormat.GPX -> "GPX (GPS Exchange Format)"
                                    ExportFormat.KML -> "KML (Google Earth)"
                                    ExportFormat.CSV -> "CSV (Spreadsheet)"
                                    ExportFormat.JSON -> "JSON (Data Exchange)"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Saved routes dialog.
 */
@Composable
fun SavedRoutesDialog(
    savedRoutes: List<SavedRoute>,
    onDismiss: () -> Unit,
    onLoadRoute: (Long) -> Unit,
    onDeleteRoute: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saved Routes") },
        text = {
            if (savedRoutes.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📚", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No saved routes")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedRoutes) { route ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = route.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${route.locations.size} stops • ${String.format("%.1f", route.totalDistance)} km",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(route.createdAt)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                IconButton(onClick = { onLoadRoute(route.id) }) {
                                    Text("▶️")
                                }
                                
                                IconButton(onClick = { onDeleteRoute(route.id) }) {
                                    Text("🗑️")
                                }
                            }
                        }
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
 * Helper functions for route planning.
 */
private fun getStatusText(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING -> "Pending"
        OrderStatus.PREPARING -> "Preparing"
        OrderStatus.READY -> "Ready"
        OrderStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
        OrderStatus.DELIVERED -> "Delivered"
        OrderStatus.CANCELLED -> "Cancelled"
    }
}

private fun calculateEstimatedTime(route: List<LocationEntity>): Int {
    // Simple estimation: 5 minutes per stop + 2 minutes travel time
    return if (route.isEmpty()) 0 else route.size * 7
}
