package com.example.cash_control

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Transaction::class, SavingsGoal::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao
}