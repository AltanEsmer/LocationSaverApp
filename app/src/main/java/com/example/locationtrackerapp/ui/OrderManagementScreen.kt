package com.example.locationtrackerapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Orders management screen with full CRUD operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    viewModel: OrderViewModel = viewModel(),
    showAddDialog: Boolean = false,
    onDialogDismiss: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val context = LocalContext.current
    var showAddOrderDialog by remember { mutableStateOf(showAddDialog) }
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var showEditOrderDialog by remember { mutableStateOf(false) }
    
    // Watch for changes in showAddDialog parameter
    LaunchedEffect(showAddDialog) {
        if (showAddDialog) {
            android.util.Log.d("OrderScreen", "showAddDialog changed to true, setting showAddOrderDialog = true")
            showAddOrderDialog = true
        }
    }
    
    // Show add order dialog
    if (showAddOrderDialog) {
        AddOrderDialog(
            onDismiss = { 
                showAddOrderDialog = false
                onDialogDismiss()
            },
            onSave = { customerName, customerPhone, orderNumber, notes, locationId ->
                viewModel.addOrder(customerName, customerPhone, orderNumber, notes, locationId)
                showAddOrderDialog = false
                onDialogDismiss()
            },
            availableLocations = locations
        )
    }
    
    // Show edit order dialog
    selectedOrder?.let { order ->
        if (showEditOrderDialog) {
            EditOrderDialog(
                order = order,
                onDismiss = { 
                    showEditOrderDialog = false
                    selectedOrder = null
                },
                onSave = { updatedOrder ->
                    viewModel.updateOrder(updatedOrder)
                    showEditOrderDialog = false
                    selectedOrder = null
                }
            )
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        
        // Orders list
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No orders found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add your first order",
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
                items(orders) { order ->
                    val orderLocation = order.locationId?.let { locationId ->
                        locations.find { it.id == locationId }
                    }
                    OrderItem(
                        order = order,
                        location = orderLocation,
                        onOrderClick = { 
                            selectedOrder = order
                            showEditOrderDialog = true
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateOrderStatus(order.id, newStatus)
                        },
                        onDeleteClick = {
                            viewModel.deleteOrder(order.id)
                        },
                        onOpenInMaps = { location ->
                            viewModel.openLocationInMaps(location)?.let { intent ->
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Order item composable for displaying individual orders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(
    order: OrderEntity,
    location: LocationEntity?,
    onOrderClick: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit,
    onDeleteClick: () -> Unit,
    onOpenInMaps: (LocationEntity) -> Unit = {}
) {
    Card(
        onClick = onOrderClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                    if (order.customerPhone.isNotEmpty()) {
                        Text(
                            text = order.customerPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Location information
                    location?.let { loc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Delivery Location",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = loc.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { onOpenInMaps(loc) },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Open in Maps",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Text(
                            text = "Lat: ${String.format("%.6f", loc.latitude)}, Lng: ${String.format("%.6f", loc.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(status = order.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete order",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            if (order.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = order.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatOrderDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Status change buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderStatus.values().forEach { status ->
                    if (status != order.status) {
                        FilterChip(
                            onClick = { onStatusChange(status) },
                            label = { Text(getStatusText(status)) },
                            selected = false,
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status chip for displaying order status.
 */
@Composable
fun StatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        OrderStatus.PREPARING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        OrderStatus.READY -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        OrderStatus.DELIVERED -> Color(0xFF4CAF50) to Color.White
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(24.dp)
    ) {
        Text(
            text = getStatusText(status),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Add order dialog.
 */
@Composable
fun AddOrderDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long?) -> Unit,
    availableLocations: List<LocationEntity> = emptyList()
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var orderNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Order") },
        text = {
            Column {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it; isError = false },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = { Text("Order Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Location selection
                Text(
                    text = "Delivery Location:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                if (availableLocations.isEmpty()) {
                    Text(
                        text = "No locations available. Save a location first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        availableLocations.forEach { location ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedLocationId = location.id },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedLocationId == location.id,
                                    onClick = { selectedLocationId = location.id }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = location.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Lat: ${String.format("%.6f", location.latitude)}, Lng: ${String.format("%.6f", location.longitude)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (isError) {
                    Text(
                        text = "Customer name is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (customerName.isNotEmpty()) {
                        onSave(customerName, customerPhone, orderNumber, notes, selectedLocationId)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Add Order")
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
 * Edit order dialog.
 */
@Composable
fun EditOrderDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onSave: (OrderEntity) -> Unit
) {
    var customerName by remember { mutableStateOf(order.customerName) }
    var customerPhone by remember { mutableStateOf(order.customerPhone) }
    var orderNumber by remember { mutableStateOf(order.orderNumber) }
    var notes by remember { mutableStateOf(order.notes) }
    var status by remember { mutableStateOf(order.status) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Order") },
        text = {
            Column {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = { Text("Order Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Status:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrderStatus.values().forEach { orderStatus ->
                        FilterChip(
                            onClick = { status = orderStatus },
                            label = { Text(getStatusText(orderStatus)) },
                            selected = status == orderStatus
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedOrder = order.copy(
                        customerName = customerName,
                        customerPhone = customerPhone,
                        orderNumber = orderNumber,
                        notes = notes,
                        status = status,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updatedOrder)
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

/**
 * Helper functions for order management.
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

private fun getStatusIcon(status: OrderStatus) = when (status) {
    // TODO: Fix icon references - temporarily commented out
    // OrderStatus.PENDING -> Icons.Default.Schedule
    // OrderStatus.PREPARING -> Icons.Default.Build
    // OrderStatus.READY -> Icons.Default.CheckCircle
    // OrderStatus.OUT_FOR_DELIVERY -> Icons.Default.Star
    // OrderStatus.DELIVERED -> Icons.Default.Done
    // OrderStatus.CANCELLED -> Icons.Default.Close
    OrderStatus.PENDING -> Icons.Default.Build
    OrderStatus.PREPARING -> Icons.Default.Build
    OrderStatus.READY -> Icons.Default.CheckCircle
    OrderStatus.OUT_FOR_DELIVERY -> Icons.Default.Star
    OrderStatus.DELIVERED -> Icons.Default.Done
    OrderStatus.CANCELLED -> Icons.Default.Close
}

private fun formatOrderDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
