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
                _customers.value = customers
            }
        }
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
        viewModelScope.launch {
            customerRepository.searchCustomers(query).collect { customers ->
                _customers.value = customers
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
 * UI state for customer management.
 */
data class CustomerUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
