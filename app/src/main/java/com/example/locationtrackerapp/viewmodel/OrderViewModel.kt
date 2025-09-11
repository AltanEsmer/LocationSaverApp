package com.example.locationtrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackerapp.data.LocationDatabase
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.repository.OrderRepository
import com.example.locationtrackerapp.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for order management operations.
 */
class OrderViewModel(application: Application) : AndroidViewModel(application) {
    
    private val orderRepository: OrderRepository
    private val locationRepository: LocationRepository
    
    // UI State
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()
    
    // Orders
    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()
    
    // Locations
    private val _locations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val locations: StateFlow<List<LocationEntity>> = _locations.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // All orders (for filtering)
    private val _allOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    
    init {
        val database = LocationDatabase.getDatabase(application)
        orderRepository = OrderRepository(database.orderDao())
        locationRepository = LocationRepository(database.locationDao())
        
        loadOrders()
        loadLocations()
    }
    
    /**
     * Load all orders from the database.
     */
    private fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { orders ->
                _allOrders.value = orders
                filterOrders()
            }
        }
    }
    
    /**
     * Filter orders based on search query.
     */
    private fun filterOrders() {
        val query = _searchQuery.value.lowercase()
        val allOrders = _allOrders.value
        
        val filtered = if (query.isEmpty()) {
            allOrders
        } else {
            allOrders.filter { order ->
                order.customerName.lowercase().contains(query) ||
                order.customerPhone.lowercase().contains(query) ||
                order.orderNumber.lowercase().contains(query) ||
                order.notes.lowercase().contains(query)
            }
        }
        
        _orders.value = filtered
    }
    
    /**
     * Load all locations from the database.
     */
    private fun loadLocations() {
        viewModelScope.launch {
            locationRepository.getAllLocations().collect { locations ->
                _locations.value = locations
            }
        }
    }
    
    /**
     * Add a new order.
     */
    fun addOrder(
        customerName: String,
        customerPhone: String = "",
        orderNumber: String = "",
        notes: String = "",
        locationId: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val order = OrderEntity(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    orderNumber = orderNumber,
                    notes = notes,
                    locationId = locationId
                )
                orderRepository.insertOrder(order)
                // Refresh the filtered list
                loadOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add order: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update an existing order.
     */
    fun updateOrder(order: OrderEntity) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrder(order)
                // Refresh the filtered list
                loadOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update order: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update order status.
     */
    fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, status)
                // Refresh the filtered list
                loadOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update order status: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete an order.
     */
    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            try {
                orderRepository.deleteOrderById(orderId)
                // Refresh the filtered list
                loadOrders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete order: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Search orders.
     */
    fun searchOrders(query: String) {
        _searchQuery.value = query
        filterOrders()
    }
    
    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Open location in Google Maps.
     */
    fun openLocationInMaps(location: LocationEntity): android.content.Intent? {
        return try {
            val uri = "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(${location.name})"
            android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to open maps: ${e.message}"
            )
            null
        }
    }
}

/**
 * UI state for order management.
 */
data class OrderUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
