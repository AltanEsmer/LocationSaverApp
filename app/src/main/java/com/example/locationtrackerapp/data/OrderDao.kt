package com.example.locationtrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for OrderEntity operations.
 */
@Dao
interface OrderDao {
    
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long
    
    @Update
    suspend fun updateOrder(order: OrderEntity)
    
    @Delete
    suspend fun deleteOrder(order: OrderEntity)
    
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity?
    
    @Query("SELECT * FROM orders WHERE customerName LIKE :query OR orderNumber LIKE :query ORDER BY createdAt DESC")
    fun searchOrders(query: String): Flow<List<OrderEntity>>
    
    @Query("UPDATE orders SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, status: OrderStatus, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)
    
    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    suspend fun getOrderCountByStatus(status: OrderStatus): Int
}
