#  Cash Control App

##  Overview
The Cash Control App is an Android application designed to help users manage their personal finances effectively. It focuses on tracking expenses, setting budgets, analysing spending patterns, and maintaining financial discipline through a secure and user-friendly system.

##  Features

###  User Authentication
The application ensures that financial data is kept private and secure through a dedicated login and registration system.

**How it works:**
- Users can create an account with a name, email, and password  
- Secure login using a local Room database to verify credentials  

**Purpose:**
- Protects sensitive financial data  
- Ensures only authorised users can access the application  


###  Add Monthly Goals (Budget)
This feature allows users to define their financial limits by setting specific spending goals.

**How it works:**
- Users set both a minimum and maximum monthly goal  
- Saving a budget adds to the user's total income pool  
- Triggers a "Budget Planner" achievement  

**Purpose:**
- Helps users plan their financial future  
- Acts as a cumulative financial limit  


### Create Categories
Users can organise their spending by creating specific categories.

**How it works:**
- Users name a category and allocate a specific portion of their budget to it  
- Categories are stored locally and available when adding expenses  

**Purpose:**
- Improves organisation of expenses  
- Makes tracking spending easier  


###  Detailed Expense Logging
This feature allows users to record every transaction with high accuracy.

**How it works:**
- Users enter the expense amount and select a category  
- Mandatory details include date, start time, end time, and description  
- Users can optionally take a photo of the receipt using the camera  

**Purpose:**
- Maintains a detailed financial history  
- Provides proof of transactions  



###  Strict Budget Control
The application helps prevent overspending by monitoring category balances.

**How it works:**
- Before adding an expense, the app checks the category’s remaining balance  
- If the expense exceeds the balance, the transaction is blocked  
- An "Insufficient funds" warning is displayed  

**Purpose:**
- Encourages responsible spending  
- Prevents exceeding budget limits  



### Filtered Expense History
Users can view and review all past transactions within selected timeframes.

**How it works:**
- Users select a start date and end date  
- The app displays all transactions within that period  
- Includes descriptions and receipt photos  

**Purpose:**
- Makes it easy to review financial activity  
- Supports better financial planning  



### Visual Statistics (Category Graphs)
This feature helps users analyse spending patterns visually.

**How it works:**
- The app calculates total spending per category within a selected period  
- Displays a horizontal bar graph for each category  
- Bars are scaled relative to the highest spending category  

**Purpose:**
- Helps users understand spending behaviour  
- Identifies high spending areas quickly  



### Achievement & Streak System
A gamified system designed to encourage consistent financial tracking.

**How it works:**
- Tracks consecutive days of expense logging (saving streaks)  
- Awards milestone trophies every 3 days  
- Unlocks badges such as "3-Day Saver", "Weekly Warrior", and "Saving Legend"  

**Purpose:**
- Motivates users to stay consistent  
- Makes financial tracking more engaging  



### Data Storage
Ensures that all financial data is saved locally and remains available.

**How it works:**
- Uses Room Database (SQLite) to store users and transactions  
- Uses SharedPreferences for user-specific settings  

**Purpose:**
- Prevents data loss when the app is closed  
- Allows the app to function fully offline for privacy  

**Youtube link** 

https://youtube.com/shorts/jfZoZaFOSY4?si=o0SZagvc8S7VC3fT


**Github link**


