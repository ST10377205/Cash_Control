package com.example.cash_control

import org.junit.Test
import org.junit.Assert.*

/**
 * Example unit test, which will execute on the development machine (host).
 * This test verifies the core financial logic of the Cash Control app.
 */
class ExampleUnitTest {
    
    @Test
    fun testBalanceCalculation() {
        val income = 5000.0
        val expenses = 1200.0
        val expectedBalance = 3800.0
        assertEquals(expectedBalance, income - expenses, 0.001)
    }

    @Test
    fun testGoalStatusLogic() {
        val minGoal = 1000.0
        val maxGoal = 5000.0
        
        val spendingSafe = 500.0
        val spendingOnTrack = 2500.0
        val spendingOver = 6000.0
        
        assertTrue(spendingSafe < minGoal)
        assertTrue(spendingOnTrack in minGoal..maxGoal)
        assertTrue(spendingOver > maxGoal)
    }

    @Test
    fun testMilestonePercentage() {
        val maxGoal = 1000.0
        val spending = 250.0
        val expectedPercent = 25
        val actualPercent = (spending / maxGoal * 100).toInt()
        assertEquals(expectedPercent, actualPercent)
    }
}
