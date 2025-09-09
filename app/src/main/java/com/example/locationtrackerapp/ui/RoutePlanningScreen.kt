package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import com.example.locationtrackerapp.viewmodel.RouteViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Route planning screen for delivery optimization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlanningScreen(
    viewModel: RouteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingOrders by viewModel.pendingOrders.collectAsState()
    val route by viewModel.route.collectAsState()
    var showRouteOptions by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Route summary card
        if (route.isNotEmpty()) {
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${route.size} stops • Estimated time: ${calculateEstimatedTime(route)} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { viewModel.openRouteInMaps(route) }
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open in Maps")
                        }
                        
                        TextButton(
                            onClick = { viewModel.clearRoute() }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Route")
                        }
                    }
                }
            }
        }
        
        // Pending orders section
        if (pendingOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Route options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.generateOptimizedRoute() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Route")
                }
                
                OutlinedButton(
                    onClick = { showRouteOptions = true }
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Options")
                }
            }
            
            // Pending orders list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingOrders) { order ->
                    RouteOrderItem(
                        order = order,
                        isInRoute = route.any { it.id == order.locationId },
                        onToggleRoute = { 
                            if (route.any { it.id == order.locationId }) {
                                viewModel.removeFromRoute(order.locationId!!)
                            } else {
                                order.locationId?.let { locationId ->
                                    viewModel.addToRoute(locationId)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Route options dialog
    if (showRouteOptions) {
        RouteOptionsDialog(
            onDismiss = { showRouteOptions = false },
            onApply = { maxStops, avoidTolls, optimizeFor ->
                viewModel.updateRouteOptions(maxStops, avoidTolls, optimizeFor)
                showRouteOptions = false
            }
        )
    }
}

/**
 * Route order item for displaying orders in route planning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOrderItem(
    order: OrderEntity,
    isInRoute: Boolean,
    onToggleRoute: () -> Unit
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
            // Order status indicator
            Surface(
                modifier = Modifier.size(12.dp),
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
                    fontWeight = FontWeight.Bold
                )
                
                if (order.orderNumber.isNotEmpty()) {
                    Text(
                        text = "Order #${order.orderNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = getStatusText(order.status),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Route toggle button
            IconButton(
                onClick = onToggleRoute
            ) {
                Icon(
                    // TODO: Fix icon references - temporarily using Close for both
                    Icons.Default.Close,
                    contentDescription = if (isInRoute) "Remove from route" else "Add to route",
                    tint = if (isInRoute) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Route options dialog.
 */
@Composable
fun RouteOptionsDialog(
    onDismiss: () -> Unit,
    onApply: (Int, Boolean, String) -> Unit
) {
    var maxStops by remember { mutableStateOf(10) }
    var avoidTolls by remember { mutableStateOf(false) }
    var optimizeFor by remember { mutableStateOf("time") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Route Options") },
        text = {
            Column {
                Text("Maximum stops per route:")
                Slider(
                    value = maxStops.toFloat(),
                    onValueChange = { maxStops = it.toInt() },
                    valueRange = 1f..20f,
                    steps = 18
                )
                Text("$maxStops stops")
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(maxStops, avoidTolls, optimizeFor)
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
