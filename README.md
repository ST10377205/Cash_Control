# Cash Control App – Feature Explanation

## 1. User Authentication
The application ensures that financial data is kept private and secure through a dedicated login and registration system.

**How it works:**
- Users can create an account with a name, email, and password.
- Secure login using a local Room database to verify credentials.

## 2. Add Monthly Goals (Budget)
This feature allows users to define their financial limits by setting specific spending goals.

**How it works:**
- Users set both a **Minimum** and **Maximum** monthly goal.
- Saving a budget adds to the user's total income pool.
- Triggers a "Budget Planner" achievement.

**Purpose:**
- Helps users plan their financial future.
- Acts as a cumulative financial limit.

## 3. Create Categories
Users can organize their spending by creating specific categories.

**How it works:**
- Users name a category and allocate a specific portion of their budget to it.
- Categories are stored locally and available for selection when adding expenses.

## 4. Detailed Expense Logging
Record every transaction with high precision to maintain an accurate financial history.

**How it works:**
- Users enter the expense amount and select a category.
- **Mandatory Details**: Users specify the **Date**, **Start Time**, **End Time**, and a **Description**.
- **Photo Attachment**: Users can optionally take a photo of the receipt using the camera.

## 5. Strict Budget Control
The application helps prevent overspending by monitoring category balances.

**How it works:**
- Before an expense is added, the app checks the category's remaining balance.
- If the expense exceeds the balance, the transaction is blocked with an "Insufficient funds" warning.

## 6. Filtered Expense History
View and review all past transactions within specific timeframes.

**How it works:**
- Use interactive **Date Pickers** to select a start and end date.
- The list automatically refreshes to show all transactions, descriptions, and captured receipt photos for that period.

## 7. Visual Statistics (Category Graphs)
Analyze spending patterns through visual data representation.

**How it works:**
- The app calculates the total spent per category within a selected period.
- Displays a **Horizontal Bar Graph** for each category, scaling bars relative to the highest spending area.

## 8. Achievement & Streak System
Gamified rewards to encourage consistent financial tracking.

**How it works:**
- **Saving Streaks**: Tracks how many consecutive days you've logged expenses.
- **Trophy Collection**: Earn **Milestone Trophies** for every 3 days of tracking and **Budget Planner** trophies for goal setting.
- **Badges**: Unlock badges like "3-Day Saver", "Weekly Warrior", and "Saving Legend".

## 9. Data Storage
Ensures that all financial data is saved locally and remains persistent.

**How it works:**
- Uses **Room Database (SQLite)** for permanent storage of users and transactions.
- Uses **SharedPreferences** for user-specific settings.

**Purpose:**
- Ensures data is not lost when the app is closed.
- Operates entirely offline for maximum privacy.
