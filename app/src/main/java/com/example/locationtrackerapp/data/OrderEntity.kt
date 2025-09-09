package com.example.locationtrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity class representing a delivery order.
 * 
 * @property id Unique identifier for the order
 * @property customerName Name of the customer
 * @property customerPhone Customer phone number
 * @property orderNumber Order reference number
 * @property status Current status of the order
 * @property notes Additional notes about the order
 * @property createdAt When the order was created
 * @property updatedAt When the order was last updated
 * @property locationId Reference to the delivery location
 */
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["locationId"])]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String = "",
    val orderNumber: String = "",
    val status: OrderStatus = OrderStatus.PENDING,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val locationId: Long? = null
)

/**
 * Enum representing the status of an order.
 */
enum class OrderStatus {
    PENDING,        // Sipariş alındı
    PREPARING,      // Hazırlanıyor
    READY,          // Hazır
    OUT_FOR_DELIVERY, // Yolda
    DELIVERED,      // Teslim edildi
    CANCELLED       // İptal edildi
}
