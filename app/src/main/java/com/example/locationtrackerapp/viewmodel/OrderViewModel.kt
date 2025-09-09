package com.example.locationtrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackerapp.data.LocationDatabase
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import com.example.locationtrackerapp.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for order management operations.
 */
class OrderViewModel(application: Application) : AndroidViewModel(application) {
    
    private val orderRepository: OrderRepository
    
    // UI State
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()
    
    // Orders
    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()
    
    init {
        val database = LocationDatabase.getDatabase(application)
        orderRepository = OrderRepository(database.orderDao())
        
        loadOrders()
    }
    
    /**
     * Load all orders from the database.
     */
    private fun loadOrders() {
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { orders ->
                _orders.value = orders
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
        viewModelScope.launch {
            orderRepository.searchOrders(query).collect { orders ->
                _orders.value = orders
            }
        }
    }
    
    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for order management.
 */
data class OrderUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
