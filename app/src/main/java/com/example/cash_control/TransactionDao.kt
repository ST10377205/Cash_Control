package com.example.cash_control

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC, startTime DESC")
    suspend fun getTransactionsForUser(email: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userEmail = :email AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    suspend fun getTransactionsInRange(email: String, startDate: String, endDate: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC, startTime DESC LIMIT 3")
    suspend fun getRecentTransactions(email: String): List<Transaction>

    @Query("SELECT COUNT(*) FROM transactions WHERE userEmail = :email")
    suspend fun getTransactionCount(email: String): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userEmail = :email AND type = :type AND date LIKE :yearMonth || '%'")
    suspend fun getMonthlySum(email: String, type: String, yearMonth: String): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userEmail = :email AND type = :type")
    suspend fun getTotalSum(email: String, type: String): Double

    @Query("SELECT * FROM transactions WHERE userEmail = :email AND date LIKE :yearMonth || '%' ORDER BY date DESC, startTime DESC")
    suspend fun getMonthlyTransactions(email: String, yearMonth: String): List<Transaction>
}
