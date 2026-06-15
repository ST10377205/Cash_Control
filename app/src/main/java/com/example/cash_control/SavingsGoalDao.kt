package com.example.cash_control

import androidx.room.*

@Dao
interface SavingsGoalDao {
    @Insert
    suspend fun insertGoal(goal: SavingsGoal)

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals WHERE userEmail = :email")
    suspend fun getGoalsForUser(email: String): List<SavingsGoal>
}
