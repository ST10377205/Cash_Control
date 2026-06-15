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
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String? = null,
    val imageUri: String? = null,
    val type: String = "expense"
)
