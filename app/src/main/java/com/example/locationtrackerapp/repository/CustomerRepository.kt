package com.example.locationtrackerapp.repository

import com.example.locationtrackerapp.data.CustomerDao
import com.example.locationtrackerapp.data.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for customer data operations.
 */
class CustomerRepository(private val customerDao: CustomerDao) {
    
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)
    
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    
    fun getFavoriteCustomers(): Flow<List<CustomerEntity>> = customerDao.getFavoriteCustomers()
    
    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    
    suspend fun incrementOrderCount(id: Long, timestamp: Long = System.currentTimeMillis()) = 
        customerDao.incrementOrderCount(id, timestamp)
    
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) = 
        customerDao.updateFavoriteStatus(id, isFavorite)
    
    suspend fun deleteCustomerById(id: Long) = customerDao.deleteCustomerById(id)
}
