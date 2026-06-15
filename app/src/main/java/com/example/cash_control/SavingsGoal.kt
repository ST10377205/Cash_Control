package com.example.cash_control

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val goalName: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0
)
