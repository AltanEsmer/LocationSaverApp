package com.example.locationtrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a customer profile.
 * 
 * @property id Unique identifier for the customer
 * @property name Customer's name
 * @property phone Customer's phone number
 * @property email Customer's email (optional)
 * @property address Default delivery address
 * @property notes Additional notes about the customer
 * @property isFavorite Whether this is a favorite customer
 * @property totalOrders Total number of orders from this customer
 * @property lastOrderDate Date of the last order
 * @property createdAt When the customer was added
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val totalOrders: Int = 0,
    val lastOrderDate: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
