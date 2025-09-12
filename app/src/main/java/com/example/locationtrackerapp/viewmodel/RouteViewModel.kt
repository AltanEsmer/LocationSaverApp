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
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * ViewModel for route planning operations with advanced optimization features.
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
    
    // Final destination
    private val _finalDestination = MutableStateFlow<LocationEntity?>(null)
    val finalDestination: StateFlow<LocationEntity?> = _finalDestination.asStateFlow()
    
    // Complete path (route + final destination)
    private val _completePath = MutableStateFlow<List<LocationEntity>>(emptyList())
    val completePath: StateFlow<List<LocationEntity>> = _completePath.asStateFlow()
    
    // Current location
    private val _currentLocation = MutableStateFlow<LocationEntity?>(null)
    val currentLocation: StateFlow<LocationEntity?> = _currentLocation.asStateFlow()
    
    // Route analytics
    private val _routeAnalytics = MutableStateFlow(RouteAnalytics())
    val routeAnalytics: StateFlow<RouteAnalytics> = _routeAnalytics.asStateFlow()
    
    // Saved routes
    private val _savedRoutes = MutableStateFlow<List<SavedRoute>>(emptyList())
    val savedRoutes: StateFlow<List<SavedRoute>> = _savedRoutes.asStateFlow()
    
    // Route options
    private var maxStops = 10
    private var avoidTolls = false
    private var optimizeFor = "time"
    private var avoidHighways = false
    private var avoidFerries = false
    private var optimizeStrategy = OptimizationStrategy.NEAREST_NEIGHBOR
    private var timeWindows = mutableMapOf<Long, Pair<Long, Long>>() // locationId to (start, end) time
    private var vehicleCapacity = 100 // kg
    private var currentLoad = 0 // kg
    
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
     * Generate optimized route for selected orders with advanced algorithms.
     */
    fun generateOptimizedRoute() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val selectedOrders = _pendingOrders.value.filter { order ->
                    order.locationId != null && _route.value.any { it.id == order.locationId }
                }
                
                if (selectedOrders.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "No orders selected for route planning",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Get locations for selected orders
                val locations = selectedOrders.mapNotNull { order ->
                    order.locationId?.let { locationId ->
                        locationRepository.getLocationById(locationId)
                    }
                }
                
                if (locations.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "No valid locations found for selected orders",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Detect final destination (farthest location from center)
                val finalDest = detectFinalDestination(locations)
                _finalDestination.value = finalDest
                
                // Apply optimization strategy
                val optimizedRoute = when (optimizeStrategy) {
                    OptimizationStrategy.NEAREST_NEIGHBOR -> optimizeRouteNearestNeighbor(locations)
                    OptimizationStrategy.GENETIC_ALGORITHM -> optimizeRouteGenetic(locations)
                    OptimizationStrategy.SIMULATED_ANNEALING -> optimizeRouteSimulatedAnnealing(locations)
                    OptimizationStrategy.TABU_SEARCH -> optimizeRouteTabuSearch(locations)
                }
                
                _route.value = optimizedRoute
                
                // Create complete path (route + final destination)
                val completePath = if (finalDest != null) {
                    optimizedRoute + finalDest
                } else {
                    optimizedRoute
                }
                _completePath.value = completePath
                
                updateRouteAnalytics(completePath)
                _uiState.value = _uiState.value.copy(isLoading = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate route: ${e.message}",
                    isLoading = false
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
        _finalDestination.value = null
        _completePath.value = emptyList()
    }
    
    /**
     * Set final destination manually.
     */
    fun setFinalDestination(locationId: Long) {
        viewModelScope.launch {
            try {
                val location = locationRepository.getLocationById(locationId)
                if (location != null) {
                    _finalDestination.value = location
                    updateCompletePath()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to set final destination: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Set current location for route calculation.
     */
    fun setCurrentLocation(latitude: Double, longitude: Double, address: String = "") {
        viewModelScope.launch {
            try {
                val currentLoc = LocationEntity(
                    id = -1, // Special ID for current location
                    name = "Current Location",
                    address = address.ifEmpty { "Current Location" },
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis()
                )
                _currentLocation.value = currentLoc
                updateCompletePath()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to set current location: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Detect final destination based on various criteria.
     */
    private fun detectFinalDestination(locations: List<LocationEntity>): LocationEntity? {
        if (locations.isEmpty()) return null
        
        // Strategy 1: Find the farthest location from the center of all locations
        val centerLat = locations.map { it.latitude }.average()
        val centerLon = locations.map { it.longitude }.average()
        
        val farthestLocation = locations.maxByOrNull { location ->
            calculateDistance(centerLat, centerLon, location.latitude, location.longitude)
        }
        
        // Strategy 2: If there's a location with "FINAL" or "DESTINATION" in the name, use that
        val finalLocation = locations.find { location ->
            location.name.uppercase().contains("FINAL") || 
            location.name.uppercase().contains("DESTINATION") ||
            location.name.uppercase().contains("END")
        }
        
        return finalLocation ?: farthestLocation
    }
    
    /**
     * Update complete path when route or final destination changes.
     */
    private fun updateCompletePath() {
        val path = mutableListOf<LocationEntity>()
        
        // Add current location if available
        _currentLocation.value?.let { currentLoc ->
            path.add(currentLoc)
        }
        
        // Add route stops
        path.addAll(_route.value)
        
        // Add final destination if available
        _finalDestination.value?.let { finalDest ->
            path.add(finalDest)
        }
        
        _completePath.value = path
        updateRouteAnalytics(path)
    }
    
    /**
     * Open route in Google Maps.
     */
    fun openRouteInMaps(route: List<LocationEntity>? = null): Intent? {
        return try {
            val pathToUse = route ?: _completePath.value
            if (pathToUse.isEmpty()) return null
            
            val waypoints = pathToUse.joinToString("|") { location ->
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
     * Nearest Neighbor optimization algorithm.
     */
    private suspend fun optimizeRouteNearestNeighbor(locations: List<LocationEntity>): List<LocationEntity> {
        if (locations.isEmpty()) return emptyList()
        
        val optimized = mutableListOf<LocationEntity>()
        val remaining = locations.toMutableList()
        
        // Start with the first location
        var current = remaining.removeAt(0)
        optimized.add(current)
        
        while (remaining.isNotEmpty()) {
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
     * Genetic Algorithm optimization for complex routes.
     */
    private suspend fun optimizeRouteGenetic(locations: List<LocationEntity>): List<LocationEntity> {
        if (locations.size <= 2) return locations
        
        val populationSize = 50
        val generations = 100
        val mutationRate = 0.1
        
        var population = generateInitialPopulation(locations, populationSize)
        
        repeat(generations) {
            val newPopulation = mutableListOf<List<LocationEntity>>()
            
            // Elitism: keep best 10% of population
            val eliteCount = (populationSize * 0.1).toInt()
            val sortedPopulation = population.sortedBy { calculateRouteCost(it) }
            newPopulation.addAll(sortedPopulation.take(eliteCount))
            
            // Generate offspring
            while (newPopulation.size < populationSize) {
                val parent1 = tournamentSelection(population)
                val parent2 = tournamentSelection(population)
                val offspring = crossover(parent1, parent2)
                val mutatedOffspring = if (Math.random() < mutationRate) mutate(offspring) else offspring
                newPopulation.add(mutatedOffspring)
            }
            
            population = newPopulation
        }
        
        return population.minByOrNull { calculateRouteCost(it) } ?: locations
    }
    
    /**
     * Simulated Annealing optimization.
     */
    private suspend fun optimizeRouteSimulatedAnnealing(locations: List<LocationEntity>): List<LocationEntity> {
        if (locations.size <= 2) return locations
        
        var currentRoute = locations.toMutableList()
        var bestRoute = currentRoute.toList()
        var temperature = 1000.0
        val coolingRate = 0.95
        
        repeat(1000) {
            val newRoute = generateNeighbor(currentRoute)
            val currentCost = calculateRouteCost(currentRoute)
            val newCost = calculateRouteCost(newRoute)
            
            if (newCost < currentCost || Math.random() < exp((currentCost - newCost) / temperature)) {
                currentRoute = newRoute.toMutableList()
                if (calculateRouteCost(currentRoute) < calculateRouteCost(bestRoute)) {
                    bestRoute = currentRoute.toMutableList()
                }
            }
            
            temperature *= coolingRate
        }
        
        return bestRoute
    }
    
    /**
     * Tabu Search optimization.
     */
    private suspend fun optimizeRouteTabuSearch(locations: List<LocationEntity>): List<LocationEntity> {
        if (locations.size <= 2) return locations
        
        var currentRoute = locations.toMutableList()
        var bestRoute = currentRoute.toList()
        val tabuList = mutableSetOf<String>()
        val tabuListSize = 10
        
        repeat(100) {
            val neighbors = generateAllNeighbors(currentRoute)
            val validNeighbors = neighbors.filter { neighbor ->
                val routeString = neighbor.joinToString(",") { it.id.toString() }
                !tabuList.contains(routeString)
            }
            
            if (validNeighbors.isNotEmpty()) {
                val bestNeighbor = validNeighbors.minByOrNull { calculateRouteCost(it) } ?: currentRoute
                currentRoute = bestNeighbor.toMutableList()
                
                if (calculateRouteCost(currentRoute) < calculateRouteCost(bestRoute)) {
                    bestRoute = currentRoute.toList()
                }
                
                val routeString = currentRoute.joinToString(",") { it.id.toString() }
                tabuList.add(routeString)
                if (tabuList.size > tabuListSize) {
                    tabuList.remove(tabuList.first())
                }
            }
        }
        
        return bestRoute
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula.
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
    
    /**
     * Calculate total cost of a route.
     */
    private fun calculateRouteCost(route: List<LocationEntity>): Double {
        if (route.size <= 1) return 0.0
        
        var totalCost = 0.0
        for (i in 0 until route.size - 1) {
            val distance = calculateDistance(
                route[i].latitude, route[i].longitude,
                route[i + 1].latitude, route[i + 1].longitude
            )
            totalCost += if (optimizeFor == "time") distance * 1.5 else distance
        }
        return totalCost
    }
    
    /**
     * Generate initial population for genetic algorithm.
     */
    private fun generateInitialPopulation(locations: List<LocationEntity>, size: Int): List<List<LocationEntity>> {
        val population = mutableListOf<List<LocationEntity>>()
        repeat(size) {
            population.add(locations.shuffled())
        }
        return population
    }
    
    /**
     * Tournament selection for genetic algorithm.
     */
    private fun tournamentSelection(population: List<List<LocationEntity>>): List<LocationEntity> {
        val tournamentSize = 3
        val tournament = population.shuffled().take(tournamentSize)
        return tournament.minByOrNull { calculateRouteCost(it) } ?: population.first()
    }
    
    /**
     * Crossover operation for genetic algorithm.
     */
    private fun crossover(parent1: List<LocationEntity>, parent2: List<LocationEntity>): List<LocationEntity> {
        if (parent1.size <= 2) return parent1
        
        val start = (0 until parent1.size / 2).random()
        val end = (start + 1 until parent1.size).random()
        
        val child = parent1.toMutableList()
        val parent2Remaining = parent2.filter { !child.subList(start, end).contains(it) }
        
        var insertIndex = 0
        for (i in parent2Remaining.indices) {
            if (insertIndex == start) insertIndex = end
            if (insertIndex < child.size) {
                child[insertIndex] = parent2Remaining[i]
                insertIndex++
            }
        }
        
        return child
    }
    
    /**
     * Mutation operation for genetic algorithm.
     */
    private fun mutate(route: List<LocationEntity>): List<LocationEntity> {
        if (route.size <= 2) return route
        
        val mutated = route.toMutableList()
        val i = (0 until route.size).random()
        val j = (0 until route.size).random()
        
        if (i != j) {
            val temp = mutated[i]
            mutated[i] = mutated[j]
            mutated[j] = temp
        }
        
        return mutated
    }
    
    /**
     * Generate neighbor for simulated annealing.
     */
    private fun generateNeighbor(route: List<LocationEntity>): List<LocationEntity> {
        if (route.size <= 2) return route
        
        val neighbor = route.toMutableList()
        val i = (0 until route.size).random()
        val j = (0 until route.size).random()
        
        if (i != j) {
            val temp = neighbor[i]
            neighbor[i] = neighbor[j]
            neighbor[j] = temp
        }
        
        return neighbor
    }
    
    /**
     * Generate all neighbors for tabu search.
     */
    private fun generateAllNeighbors(route: List<LocationEntity>): List<List<LocationEntity>> {
        if (route.size <= 2) return listOf(route)
        
        val neighbors = mutableListOf<List<LocationEntity>>()
        for (i in route.indices) {
            for (j in i + 1 until route.size) {
                val neighbor = route.toMutableList()
                val temp = neighbor[i]
                neighbor[i] = neighbor[j]
                neighbor[j] = temp
                neighbors.add(neighbor)
            }
        }
        return neighbors
    }
    
    /**
     * Update route analytics.
     */
    private fun updateRouteAnalytics(route: List<LocationEntity>) {
        val totalDistance = calculateRouteCost(route)
        val estimatedTime = (totalDistance * 1.5).toInt() // 1.5 minutes per km
        val efficiency = if (route.size > 1) totalDistance / route.size else 0.0
        
        _routeAnalytics.value = RouteAnalytics(
            totalDistance = totalDistance,
            estimatedTime = estimatedTime,
            totalStops = route.size,
            efficiency = efficiency,
            averageDistancePerStop = if (route.size > 1) totalDistance / route.size else 0.0
        )
    }
    
    /**
     * Save current route with a name.
     */
    fun saveRoute(name: String) {
        viewModelScope.launch {
            try {
                val savedRoute = SavedRoute(
                    id = System.currentTimeMillis(),
                    name = name,
                    locations = _route.value,
                    createdAt = System.currentTimeMillis(),
                    totalDistance = _routeAnalytics.value.totalDistance,
                    estimatedTime = _routeAnalytics.value.estimatedTime
                )
                _savedRoutes.value = _savedRoutes.value + savedRoute
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save route: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Load a saved route.
     */
    fun loadRoute(routeId: Long) {
        viewModelScope.launch {
            try {
                val savedRoute = _savedRoutes.value.find { it.id == routeId }
                if (savedRoute != null) {
                    _route.value = savedRoute.locations
                    updateRouteAnalytics(savedRoute.locations)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load route: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete a saved route.
     */
    fun deleteRoute(routeId: Long) {
        _savedRoutes.value = _savedRoutes.value.filter { it.id != routeId }
    }
    
    /**
     * Export route to various formats.
     */
    fun exportRoute(format: ExportFormat): String {
        return when (format) {
            ExportFormat.GPX -> generateGPX(_route.value)
            ExportFormat.KML -> generateKML(_route.value)
            ExportFormat.CSV -> generateCSV(_route.value)
            ExportFormat.JSON -> generateJSON(_route.value)
        }
    }
    
    /**
     * Generate GPX format.
     */
    private fun generateGPX(route: List<LocationEntity>): String {
        val gpx = StringBuilder()
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        gpx.append("<gpx version=\"1.1\" creator=\"LocationSaverApp\">\n")
        gpx.append("  <trk>\n")
        gpx.append("    <name>Delivery Route</name>\n")
        gpx.append("    <trkseg>\n")
        
        route.forEach { location ->
            gpx.append("      <trkpt lat=\"${location.latitude}\" lon=\"${location.longitude}\">\n")
            gpx.append("        <name>${location.name}</name>\n")
            gpx.append("      </trkpt>\n")
        }
        
        gpx.append("    </trkseg>\n")
        gpx.append("  </trk>\n")
        gpx.append("</gpx>")
        return gpx.toString()
    }
    
    /**
     * Generate KML format.
     */
    private fun generateKML(route: List<LocationEntity>): String {
        val kml = StringBuilder()
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        kml.append("  <Document>\n")
        kml.append("    <name>Delivery Route</name>\n")
        
        route.forEachIndexed { index, location ->
            kml.append("    <Placemark>\n")
            kml.append("      <name>Stop ${index + 1}: ${location.name}</name>\n")
            kml.append("      <Point>\n")
            kml.append("        <coordinates>${location.longitude},${location.latitude},0</coordinates>\n")
            kml.append("      </Point>\n")
            kml.append("    </Placemark>\n")
        }
        
        kml.append("  </Document>\n")
        kml.append("</kml>")
        return kml.toString()
    }
    
    /**
     * Generate CSV format.
     */
    private fun generateCSV(route: List<LocationEntity>): String {
        val csv = StringBuilder()
        csv.append("Stop,Name,Latitude,Longitude,Address\n")
        
        route.forEachIndexed { index, location ->
            csv.append("${index + 1},${location.name},${location.latitude},${location.longitude},${location.address}\n")
        }
        
        return csv.toString()
    }
    
    /**
     * Generate JSON format.
     */
    private fun generateJSON(route: List<LocationEntity>): String {
        val json = StringBuilder()
        json.append("{\n")
        json.append("  \"route\": {\n")
        json.append("    \"name\": \"Delivery Route\",\n")
        json.append("    \"totalStops\": ${route.size},\n")
        json.append("    \"totalDistance\": ${_routeAnalytics.value.totalDistance},\n")
        json.append("    \"estimatedTime\": ${_routeAnalytics.value.estimatedTime},\n")
        json.append("    \"stops\": [\n")
        
        route.forEachIndexed { index, location ->
            json.append("      {\n")
            json.append("        \"stop\": ${index + 1},\n")
            json.append("        \"name\": \"${location.name}\",\n")
            json.append("        \"latitude\": ${location.latitude},\n")
            json.append("        \"longitude\": ${location.longitude},\n")
            json.append("        \"address\": \"${location.address}\"\n")
            json.append("      }${if (index < route.size - 1) "," else ""}\n")
        }
        
        json.append("    ]\n")
        json.append("  }\n")
        json.append("}")
        return json.toString()
    }
    
    /**
     * Update route options with advanced settings.
     */
    fun updateAdvancedRouteOptions(
        maxStops: Int,
        avoidTolls: Boolean,
        avoidHighways: Boolean,
        avoidFerries: Boolean,
        optimizeFor: String,
        strategy: OptimizationStrategy,
        vehicleCapacity: Int
    ) {
        this.maxStops = maxStops
        this.avoidTolls = avoidTolls
        this.avoidHighways = avoidHighways
        this.avoidFerries = avoidFerries
        this.optimizeFor = optimizeFor
        this.optimizeStrategy = strategy
        this.vehicleCapacity = vehicleCapacity
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

/**
 * Route analytics data.
 */
data class RouteAnalytics(
    val totalDistance: Double = 0.0,
    val estimatedTime: Int = 0,
    val totalStops: Int = 0,
    val efficiency: Double = 0.0,
    val averageDistancePerStop: Double = 0.0
)

/**
 * Saved route data.
 */
data class SavedRoute(
    val id: Long,
    val name: String,
    val locations: List<LocationEntity>,
    val createdAt: Long,
    val totalDistance: Double,
    val estimatedTime: Int
)

/**
 * Optimization strategies.
 */
enum class OptimizationStrategy {
    NEAREST_NEIGHBOR,
    GENETIC_ALGORITHM,
    SIMULATED_ANNEALING,
    TABU_SEARCH
}

/**
 * Export formats.
 */
enum class ExportFormat {
    GPX,
    KML,
    CSV,
    JSON
}
