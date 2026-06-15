# Final POE - Part 3 (Final App Development)

**Project Name:** Cash Control  
**Course:** [Insert Course Name Here]  
**Student Name:** [Your Name Here]  

---

## Project Links
*   **GitHub Repository**: [Insert Link Here]
*   **YouTube Demo Video**: [Insert Link Here]

---

## Overview
Cash Control is a professional Android application designed to help users manage their finances with precision. This version represents the Final POE (Part 3), featuring an offline-first architecture with real-time cloud synchronization via Firebase Firestore, advanced visual analytics, and gamified achievements.

### Target Audience
The app is tailored for young professionals and students who seek to gain control over their spending habits. It serves users who need a centralized tool to track income, manage budgets across categories, and stay motivated through visual progress and rewards.

---

## Design Considerations
*   **User Interface (UI)**: I adopted a clean, Material 3 design philosophy. High-contrast colors are utilized for critical financial indicators—Green for income and "Safe" zones, and Red for overspending—to provide immediate visual feedback.
*   **Architecture (Offline-First)**: To ensure reliability, the app follows a Repository Pattern. Data is first saved to a local **Room** database, ensuring functionality without internet, and then synchronized with **Firebase Firestore** when a connection is available.
*   **Gamification**: The streak and badge system was designed to leverage positive reinforcement. By rewarding daily logging, the app encourages the habit of financial mindfulness.

---

## Custom Own Features (Requirement)
In addition to the core project requirements, the following two unique features were designed and implemented:

### 1. Savings Goals (Set Goal)
Beyond simple expense tracking, users can define long-term financial targets. This feature allows users to set a specific target amount for goals like a Holiday or Emergency Fund and track their progress over time, encouraging a healthy saving mindset.

### 2. Financial Knowledge Hub
An educational center within the app that provides users with curated financial tips, investment basics, and budgeting strategies. This transforms the app from a simple calculator into a tool for long-term financial growth and literacy.

---

## Main Features

### 1. Secure Authentication and Cloud Sync
*   **Firebase Auth**: Secure account creation and login system.
*   **Cloud Backup and Sync**: All user data, profiles, and transactions are securely stored in Firebase Firestore, allowing for cross-device synchronization.
*   **Smart Recovery**: A robust Forgot Password flow ensures users can recover access via their registered email.

### 2. Intelligent Dashboard and Gamification
*   **Lifetime Balance**: A Digital Wallet balance that carries over indefinitely (Total Income - Total Expenses).
*   **Visual Goal Tracking**: Color-coded feedback (Safe, On Track, Overspending) based on staying between minimum and maximum goals for the past month.
*   **Milestone Rewards**: Visual progress dots on the dashboard that unlock as users stay within their budget (25%, 50%, 75%, 100%).
*   **Streak System**: A dedicated Streak page that rewards consistent daily logging with badges and trophies.

### 3. Precision Transaction Management
*   **Digital Receipts**: Users can attach photos of receipts using the camera or gallery. Images are processed and stored locally for easy review.
*   **Rich Details**: Log precise times, dates, categories, and descriptions. All entries are automatically synced to the cloud.

### 4. Visual Insights and Analytics (Stats)
*   **Dynamic Charts**: Beautiful Pie and Bar charts using MPAndroidChart to show spending per category.
*   **Goal Overlays**: The bar chart includes visual Limit Lines for Min/Max goals, showing exactly how spending compares to targets over a user-selectable period.

---

## GitHub and Version Control
### Version Control Strategy
*   **Regular Commits**: I maintained a frequent commit schedule to document the iterative development of features like the Room database and Firebase integration.
*   **README Documentation**: The README serves as the primary technical guide for the project, detailing both usage and architecture.

### GitHub Actions (CI/CD)
This repository is configured with **GitHub Actions** (`android.yml`) to automatically build the application and run automated tests on every push. This ensures:
1.  The application compiles correctly on a clean environment.
2.  Core logic, such as balance calculations and goal status math, is verified through unit tests.

---

## How to Use the App
1.  **Sign Up**: Create an account to start syncing data to the cloud.
2.  **Set Goals**: Use "Set Budget" to define your spending limits.
3.  **Log Transactions**: Tap "Add Expense" to record spending or income. Don't forget to snap a photo of your receipt!
4.  **Monitor Stats**: Check the "Stats" tab to see category breakdowns and compare spending against your limit lines.
5.  **Build a Streak**: Log daily to earn the "Saving Legend" badge on the Streak page.

---

## Technical Stack
*   **Language**: Kotlin
*   **Local Database**: Room (SQLite) for high-performance offline access.
*   **Cloud Database**: Firebase Firestore for real-time cloud storage and sync.
*   **UI Components**: Material Design 3, CardViews, and MPAndroidChart.
*   **Concurrency**: Kotlin Coroutines for smooth background operations.

---

## Documentation
The following documents are submitted as part of this project:
*   [Research Document](./docs/Research_Document.pdf)
*   [Design Document](./docs/Design_Document.pdf)
*(Note: Ensure these files are placed in the docs folder of your repository.)*

---
**Cash Control** - Master your money, one transaction at a time.
