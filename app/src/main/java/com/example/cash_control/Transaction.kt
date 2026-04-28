package com.example.cash_control

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val category: String,
    val amount: Double,
    val date: String,
    val imageUri: String? = null // Store the URI or file path of the image
)
