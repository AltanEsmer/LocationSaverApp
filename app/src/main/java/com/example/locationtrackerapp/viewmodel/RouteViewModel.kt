package com.example.locationtrackerapp.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackerapp.data.LocationDatabase
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import com.example.locationtrackerapp.repository.OrderRepository
import com.example.locationtrackerapp.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for route planning operations.
 */
class RouteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val orderRepository: OrderRepository
    private val locationRepository: LocationRepository
    
    // UI State
    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()
    
    // Pending orders
    private val _pendingOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val pendingOrders: StateFlow<List<OrderEntity>> = _pendingOrders.asStateFlow()
    
    // Current route
    private val _route = MutableStateFlow<List<LocationEntity>>(emptyList())
    val route: StateFlow<List<LocationEntity>> = _route.asStateFlow()
    
    // Route options
    private var maxStops = 10
    private var avoidTolls = false
    private var optimizeFor = "time"
    
    init {
        val database = LocationDatabase.getDatabase(application)
        orderRepository = OrderRepository(database.orderDao())
        locationRepository = LocationRepository(database.locationDao())
        
        loadPendingOrders()
    }
    
    /**
     * Load pending orders (PREPARING and READY status).
     */
    private fun loadPendingOrders() {
        viewModelScope.launch {
            orderRepository.getOrdersByStatus(OrderStatus.PREPARING).collect { preparingOrders ->
                orderRepository.getOrdersByStatus(OrderStatus.READY).collect { readyOrders ->
                    _pendingOrders.value = preparingOrders + readyOrders
                }
            }
        }
    }
    
    /**
     * Generate optimized route for selected orders.
     */
    fun generateOptimizedRoute() {
        viewModelScope.launch {
            try {
                val selectedOrders = _pendingOrders.value.filter { order ->
                    order.locationId != null && _route.value.any { it.id == order.locationId }
                }
                
                if (selectedOrders.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "No orders selected for route planning"
                    )
                    return@launch
                }
                
                // Get locations for selected orders
                val locations = selectedOrders.mapNotNull { order ->
                    order.locationId?.let { locationId ->
                        locationRepository.getLocationById(locationId)
                    }
                }
                
                // Simple optimization: sort by distance from a central point
                val optimizedRoute = optimizeRoute(locations)
                _route.value = optimizedRoute
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate route: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Add location to route.
     */
    fun addToRoute(locationId: Long) {
        viewModelScope.launch {
            try {
                val location = locationRepository.getLocationById(locationId)
                if (location != null && !_route.value.any { it.id == locationId }) {
                    _route.value = _route.value + location
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add location to route: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Remove location from route.
     */
    fun removeFromRoute(locationId: Long) {
        _route.value = _route.value.filter { it.id != locationId }
    }
    
    /**
     * Clear current route.
     */
    fun clearRoute() {
        _route.value = emptyList()
    }
    
    /**
     * Open route in Google Maps.
     */
    fun openRouteInMaps(route: List<LocationEntity>): Intent? {
        return try {
            if (route.isEmpty()) return null
            
            val waypoints = route.joinToString("|") { location ->
                "${location.latitude},${location.longitude}"
            }
            
            val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&waypoints=$waypoints")
            Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to open route in Maps: ${e.message}"
            )
            null
        }
    }
    
    /**
     * Update route options.
     */
    fun updateRouteOptions(maxStops: Int, avoidTolls: Boolean, optimizeFor: String) {
        this.maxStops = maxStops
        this.avoidTolls = avoidTolls
        this.optimizeFor = optimizeFor
    }
    
    /**
     * Simple route optimization algorithm.
     * In a real app, this would integrate with Google Maps API or similar service.
     */
    private suspend fun optimizeRoute(locations: List<LocationEntity>): List<LocationEntity> {
        if (locations.isEmpty()) return emptyList()
        
        // Simple nearest neighbor algorithm
        val optimized = mutableListOf<LocationEntity>()
        val remaining = locations.toMutableList()
        
        // Start with the first location
        var current = remaining.removeAt(0)
        optimized.add(current)
        
        while (remaining.isNotEmpty()) {
            // Find the nearest location to the current one
            val nearest = remaining.minByOrNull { location ->
                calculateDistance(
                    current.latitude, current.longitude,
                    location.latitude, location.longitude
                )
            }
            
            if (nearest != null) {
                remaining.remove(nearest)
                optimized.add(nearest)
                current = nearest
            }
        }
        
        return optimized
    }
    
    /**
     * Calculate distance between two coordinates (simple approximation).
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
    
    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for route planning.
 */
data class RouteUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
