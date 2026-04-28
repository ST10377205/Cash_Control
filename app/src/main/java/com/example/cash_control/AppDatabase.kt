package com.example.cash_control

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Transaction::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
}