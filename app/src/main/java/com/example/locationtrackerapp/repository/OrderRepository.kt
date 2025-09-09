package com.example.locationtrackerapp.repository

import com.example.locationtrackerapp.data.OrderDao
import com.example.locationtrackerapp.data.OrderEntity
import com.example.locationtrackerapp.data.OrderStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for order data operations.
 */
class OrderRepository(private val orderDao: OrderDao) {
    
    suspend fun insertOrder(order: OrderEntity): Long = orderDao.insertOrder(order)
    
    suspend fun updateOrder(order: OrderEntity) = orderDao.updateOrder(order)
    
    suspend fun deleteOrder(order: OrderEntity) = orderDao.deleteOrder(order)
    
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()
    
    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>> = orderDao.getOrdersByStatus(status)
    
    suspend fun getOrderById(id: Long): OrderEntity? = orderDao.getOrderById(id)
    
    fun searchOrders(query: String): Flow<List<OrderEntity>> = orderDao.searchOrders(query)
    
    suspend fun updateOrderStatus(id: Long, status: OrderStatus, timestamp: Long = System.currentTimeMillis()) = 
        orderDao.updateOrderStatus(id, status, timestamp)
    
    suspend fun deleteOrderById(id: Long) = orderDao.deleteOrderById(id)
    
    suspend fun getOrderCountByStatus(status: OrderStatus): Int = orderDao.getOrderCountByStatus(status)
}
