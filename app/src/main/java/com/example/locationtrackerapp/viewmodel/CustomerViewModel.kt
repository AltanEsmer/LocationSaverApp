package com.example.locationtrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtrackerapp.data.CustomerEntity
import com.example.locationtrackerapp.data.LocationDatabase
import com.example.locationtrackerapp.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for customer management operations.
 */
class CustomerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val customerRepository: CustomerRepository
    
    // UI State
    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()
    
    // Customers
    private val _customers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    val customers: StateFlow<List<CustomerEntity>> = _customers.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // All customers (for filtering)
    private val _allCustomers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    
    init {
        val database = LocationDatabase.getDatabase(application)
        customerRepository = CustomerRepository(database.customerDao())
        
        loadCustomers()
    }
    
    /**
     * Load all customers from the database.
     */
    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _allCustomers.value = customers
                filterCustomers()
            }
        }
    }
    
    /**
     * Filter customers based on search query.
     */
    private fun filterCustomers() {
        val query = _searchQuery.value.lowercase()
        val allCustomers = _allCustomers.value
        
        val filtered = if (query.isEmpty()) {
            allCustomers
        } else {
            allCustomers.filter { customer ->
                customer.name.lowercase().contains(query) ||
                customer.phone.lowercase().contains(query) ||
                customer.email.lowercase().contains(query) ||
                customer.address.lowercase().contains(query) ||
                customer.notes.lowercase().contains(query)
            }
        }
        
        _customers.value = filtered
    }
    
    /**
     * Add a new customer.
     */
    fun addCustomer(
        name: String,
        phone: String = "",
        email: String = "",
        address: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val customer = CustomerEntity(
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    notes = notes
                )
                customerRepository.insertCustomer(customer)
                // Refresh the filtered list
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add customer: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update an existing customer.
     */
    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            try {
                customerRepository.updateCustomer(customer)
                // Refresh the filtered list
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update customer: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Toggle favorite status for a customer.
     */
    fun toggleFavorite(customerId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                customerRepository.updateFavoriteStatus(customerId, isFavorite)
                // Refresh the filtered list
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update favorite status: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Delete a customer.
     */
    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            try {
                customerRepository.deleteCustomerById(customerId)
                // Refresh the filtered list
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete customer: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Search customers.
     */
    fun searchCustomers(query: String) {
        _searchQuery.value = query
        filterCustomers()
    }
    
    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for customer management.
 */
data class CustomerUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
