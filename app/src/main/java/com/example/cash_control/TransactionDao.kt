package com.example.cash_control

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC")
    suspend fun getTransactionsForUser(email: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userEmail = :email AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsInRange(email: String, startDate: String, endDate: String): List<Transaction>
}
