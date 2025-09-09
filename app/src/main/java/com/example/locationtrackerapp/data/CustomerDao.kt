package com.example.locationtrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CustomerEntity operations.
 */
@Dao
interface CustomerDao {
    
    @Insert
    suspend fun insertCustomer(customer: CustomerEntity): Long
    
    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
    
    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
    
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteCustomers(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE name LIKE :query OR phone LIKE :query ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>
    
    @Query("UPDATE customers SET totalOrders = totalOrders + 1, lastOrderDate = :timestamp WHERE id = :id")
    suspend fun incrementOrderCount(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE customers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)
}
