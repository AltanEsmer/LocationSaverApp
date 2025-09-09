package com.example.locationtrackerapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Order management screen for delivery tracking.
 */
@Composable
fun OrderManagementScreen(
    orders: List<OrderEntity>,
    onOrderClick: (OrderEntity) -> Unit,
    onStatusChange: (OrderEntity, OrderStatus) -> Unit,
    onDeleteOrder: (OrderEntity) -> Unit
) {
    var selectedStatus by remember { mutableStateOf<OrderStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredOrders = orders.filter { order ->
        val matchesStatus = selectedStatus == null || order.status == selectedStatus
        val matchesSearch = searchQuery.isEmpty() || 
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                order.orderNumber.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Sipariş Ara") },
            placeholder = { Text("Müşteri adı veya sipariş numarası...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { selectedStatus = null },
                label = { Text("Tümü") }
            )
            OrderStatus.values().forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = status },
                    label = { Text(getStatusDisplayName(status)) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Orders list
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz sipariş yok",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredOrders) { order ->
                    OrderItem(
                        order = order,
                        onOrderClick = onOrderClick,
                        onStatusChange = onStatusChange,
                        onDeleteOrder = onDeleteOrder
                    )
                }
            }
        }
    }
}

/**
 * Individual order item component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(
    order: OrderEntity,
    onOrderClick: (OrderEntity) -> Unit,
    onStatusChange: (OrderEntity, OrderStatus) -> Unit,
    onDeleteOrder: (OrderEntity) -> Unit
) {
    Card(
        onClick = { onOrderClick(order) },
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
                            text = "Sipariş #${order.orderNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                StatusChip(status = order.status)
            }
            
            if (order.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = order.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(order.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    IconButton(
                        onClick = { onStatusChange(order, getNextStatus(order.status)) }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Durum Güncelle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onDeleteOrder(order) }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status chip component.
 */
@Composable
fun StatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = getStatusColors(status)
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = getStatusDisplayName(status),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Get display name for order status in Turkish.
 */
private fun getStatusDisplayName(status: OrderStatus): String {
    return when (status) {
        OrderStatus.PENDING -> "Bekliyor"
        OrderStatus.PREPARING -> "Hazırlanıyor"
        OrderStatus.READY -> "Hazır"
        OrderStatus.OUT_FOR_DELIVERY -> "Yolda"
        OrderStatus.DELIVERED -> "Teslim Edildi"
        OrderStatus.CANCELLED -> "İptal"
    }
}

/**
 * Get colors for status chip.
 */
@Composable
private fun getStatusColors(status: OrderStatus): Pair<Color, Color> {
    return when (status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        OrderStatus.PREPARING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        OrderStatus.READY -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
}

/**
 * Get next status in the order flow.
 */
private fun getNextStatus(currentStatus: OrderStatus): OrderStatus {
    return when (currentStatus) {
        OrderStatus.PENDING -> OrderStatus.PREPARING
        OrderStatus.PREPARING -> OrderStatus.READY
        OrderStatus.READY -> OrderStatus.OUT_FOR_DELIVERY
        OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
        OrderStatus.DELIVERED -> OrderStatus.DELIVERED
        OrderStatus.CANCELLED -> OrderStatus.CANCELLED
    }
}

/**
 * Format timestamp to readable string.
 */
private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
