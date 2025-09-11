package com.example.locationtrackerapp.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackerapp.data.LocationDatabase
import com.example.locationtrackerapp.data.LocationEntity
import com.example.locationtrackerapp.repository.LocationRepository
import com.example.locationtrackerapp.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen of the Location Saver App.
 * Manages UI state and handles business logic for location operations.
 * 
 * This ViewModel follows the MVVM pattern and provides:
 * - Location saving functionality
 * - Location listing with reactive updates
 * - Google Maps integration
 * - Error handling and loading states
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val locationRepository: LocationRepository
    private val locationService: LocationService
    
    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // Saved locations
    private val _savedLocations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val savedLocations: StateFlow<List<LocationEntity>> = _savedLocations.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // All locations (for filtering)
    private val _allLocations = MutableStateFlow<List<LocationEntity>>(emptyList())
    
    init {
        // Initialize database and repository
        val database = LocationDatabase.getDatabase(application)
        locationRepository = LocationRepository(database.locationDao())
        locationService = LocationService(application)
        
        // Load saved locations with error handling
        try {
            loadSavedLocations()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to load saved locations: ${e.message}"
            )
        }
    }
    
    /**
     * Load all saved locations from the database.
     */
    private fun loadSavedLocations() {
        viewModelScope.launch {
            locationRepository.getAllLocations().collect { locations ->
                _allLocations.value = locations
                filterLocations()
            }
        }
    }
    
    /**
     * Filter locations based on search query.
     */
    private fun filterLocations() {
        val query = _searchQuery.value.lowercase()
        val allLocations = _allLocations.value
        
        val filtered = if (query.isEmpty()) {
            allLocations
        } else {
            allLocations.filter { location ->
                location.name.lowercase().contains(query) ||
                location.address.lowercase().contains(query) ||
                location.notes.lowercase().contains(query)
            }
        }
        
        _savedLocations.value = filtered
    }
    
    /**
     * Save the current location with a user-provided name.
     * 
     * @param name The name to save the location with
     */
    fun saveCurrentLocation(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                if (!locationService.hasLocationPermissions()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Location permissions not granted"
                    )
                    return@launch
                }
                
                val location = locationService.getFreshLocation()
                
                // Debug logging
                android.util.Log.d("LocationSaver", "Saving location: $name at ${location.latitude}, ${location.longitude}")
                
                val locationId = locationRepository.saveLocation(
                    name = name,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                // Refresh the filtered list
                loadSavedLocations()
                
                // Debug: Check all saved locations
                val allLocations = locationRepository.getAllLocationsSync()
                android.util.Log.d("LocationSaver", "All saved locations: ${allLocations.map { "${it.name}: ${it.latitude}, ${it.longitude}" }}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    lastSavedLocationId = locationId
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get location: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Open a saved location in Google Maps.
     * 
     * @param location The location to open in Google Maps
     * @return Intent for opening Google Maps, or null if location is invalid
     */
    fun openLocationInMaps(location: LocationEntity): Intent? {
        return try {
            val gmmIntentUri = Uri.parse(
                "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(${location.name})"
            )
            Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to open location in Maps: ${e.message}"
            )
            null
        }
    }
    
    /**
     * Delete a saved location.
     * 
     * @param locationId The ID of the location to delete
     */
    fun deleteLocation(locationId: Long) {
        viewModelScope.launch {
            try {
                locationRepository.deleteLocation(locationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete location: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear the last saved location ID.
     */
    fun clearLastSavedLocationId() {
        _uiState.value = _uiState.value.copy(lastSavedLocationId = null)
    }
    
    /**
     * Search locations by name or address.
     * 
     * @param query Search query
     */
    fun searchLocations(query: String) {
        _searchQuery.value = query
        filterLocations()
    }
    
    /**
     * Get current location for testing purposes.
     * 
     * @param onSuccess Callback with latitude and longitude
     * @param onError Callback with error message
     */
    fun getCurrentLocationForTesting(
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!locationService.hasLocationPermissions()) {
                    onError("Location permissions not granted")
                    return@launch
                }
                
                val location = locationService.getFreshLocation()
                onSuccess(location.latitude, location.longitude)
                
            } catch (e: Exception) {
                onError("Failed to get location: ${e.message}")
            }
        }
    }
    
    /**
     * Clear all saved locations for testing purposes.
     */
    fun clearAllLocations() {
        viewModelScope.launch {
            try {
                locationRepository.deleteAllLocations()
                android.util.Log.d("LocationSaver", "All locations cleared")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to clear locations: ${e.message}"
                )
            }
        }
    }
}

/**
 * UI state data class for the main screen.
 * 
 * @property isLoading Whether a location operation is in progress
 * @property error Any error message to display to the user
 * @property lastSavedLocationId ID of the most recently saved location
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastSavedLocationId: Long? = null
)
