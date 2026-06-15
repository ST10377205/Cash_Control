# Final POE – Part 3 (Final App Development)

## Project Information

**Project Name:** Cash Control
**Course:** OPSC6311

### Group Members

* **Kamohelo Simata** – ST10377205
* **Azwidali Manyaga** – ST10440560
* **Lusani Ramulifho** – ST10439607
* **Neo Entle Mokoena** – ST10441962

---

## Project Links

* **GitHub Repository:**
  https://github.com/ST10377205/Cash_Control/tree/master

* **YouTube Demo Video:**
  *[Insert YouTube Link Here]*

---

# Overview

**Cash Control** is a professional Android application designed to help users manage their finances efficiently and accurately. This version represents the **Final POE (Part 3)** and features an **offline-first architecture**, **real-time cloud synchronization using Firebase Firestore**, **advanced visual analytics**, and **gamified achievements** to improve user engagement.

The application simplifies financial management by helping users track income and expenses, manage budgets, visualize spending habits, and achieve financial goals.

## Target Audience

Cash Control is primarily designed for **students and young professionals** who want to improve their spending habits and gain better control of their finances. The application provides a centralized platform for tracking transactions, managing category-based budgets, and staying motivated through rewards and progress tracking.

---

# Design Considerations

## 1. User Interface (UI)

The application follows a **clean and modern Material Design 3 interface** to provide an intuitive and user-friendly experience. High-contrast colors are used strategically for financial indicators:

* **Green** → Income and safe financial zones
* **Red** → Overspending warnings and financial risk areas

This visual approach allows users to quickly understand their financial status.

## 2. Offline-First Architecture

To ensure reliability and uninterrupted access, the application follows an **Offline-First Repository Pattern**.

* Data is initially stored locally using **Room Database (SQLite)**.
* Users can continue using the app without an internet connection.
* Once internet connectivity becomes available, data is synchronized automatically with **Firebase Firestore**.

This architecture guarantees data accessibility and reliability.

## 3. Gamification

The gamification system was developed to encourage consistent financial tracking through **positive reinforcement**.

Users are rewarded for maintaining healthy financial habits through:

* Daily logging streaks
* Achievement badges
* Trophy rewards
* Budget completion milestones

---

# Custom Features (Requirement)

In addition to the required project functionality, the following unique features were designed and implemented:

## 1. Savings Goals (Set Goal)

This feature allows users to create and track **long-term financial goals**.

Users can:

* Set target savings amounts
* Monitor progress toward goals
* Stay motivated to save for objectives such as:

    * Holidays
    * Emergency funds
    * Personal purchases

This feature encourages responsible saving habits and long-term financial planning.

## 2. Financial Knowledge Hub

The **Financial Knowledge Hub** acts as an educational section within the app.

It provides users with:

* Financial management tips
* Budgeting strategies
* Investment basics
* Money-saving advice

This transforms the app from a simple finance tracker into a tool that promotes **financial literacy and long-term financial growth**.

---

# Main Features

## 1. Secure Authentication and Cloud Synchronization

### Firebase Authentication

* Secure account registration and login system.
* Protected user authentication using Firebase.

### Cloud Backup and Sync

* User profiles, transactions, and financial data are securely stored in **Firebase Firestore**.
* Supports **cross-device synchronization**.

### Password Recovery

* Built-in **Forgot Password** functionality.
* Allows users to recover accounts through registered email verification.

---

## 2. Intelligent Dashboard and Gamification

### Lifetime Balance

The application calculates a **digital wallet balance** based on:

**Total Income – Total Expenses**

This balance remains persistent throughout app usage.

### Visual Goal Tracking

Users receive **color-coded financial feedback** based on spending performance:

* **Safe** → Spending within range
* **On Track** → Financial goals are progressing well
* **Overspending** → Spending exceeds limits

### Milestone Rewards

The dashboard includes **progress milestones** that unlock at:

* 25% completion
* 50% completion
* 75% completion
* 100% completion

### Streak System

A dedicated **Streak Page** rewards users for daily activity with:

* Badges
* Achievements
* Trophy rewards

This encourages consistency and better financial habits.

---

## 3. Precision Transaction Management

### Digital Receipts

Users can upload receipt images through:

* Camera capture
* Gallery upload

Receipts are stored locally for convenient future reference.

### Detailed Transactions

Users can record:

* Expense or income amount
* Date and time
* Category
* Description

All transactions are automatically synchronized to the cloud.

---

## 4. Visual Insights and Analytics (Stats)

### Dynamic Charts

The application includes **Pie Charts** and **Bar Charts** powered by **MPAndroidChart** to visualize spending trends and category distributions.

### Goal Overlays

Bar charts include **minimum and maximum budget limit lines**, allowing users to compare spending against targets over selected time periods.

---

# GitHub and Version Control

## Version Control Strategy

### Regular Commits

The project follows a structured commit history to document the iterative development process, including:

* Room database integration
* Firebase implementation
* Dashboard improvements
* Gamification features

### README Documentation

This README serves as the project's **technical overview and user guide**, explaining the application's features, architecture, and implementation details.

---

## GitHub Actions (CI/CD)

The repository is configured with **GitHub Actions (`android.yml`)** to automate application builds and testing after every push.

This ensures:

1. The application builds successfully in a clean environment.
2. Core application logic, such as budget calculations and goal tracking, functions correctly through automated testing.

---

# How to Use the App

### Step 1: Sign Up

Create an account to securely store and synchronize your financial data.

### Step 2: Set Financial Goals

Use the **Set Budget** feature to define spending limits and financial targets.

### Step 3: Add Transactions

Select **Add Expense** to record:

* Expenses
* Income
* Categories
* Descriptions

Users can also upload receipt images for record-keeping.

### Step 4: Monitor Statistics

Navigate to the **Stats** section to:

* View spending breakdowns
* Analyze financial trends
* Compare spending against budget limits

### Step 5: Maintain a Streak

Log financial activities daily to unlock achievements such as the **Saving Legend Badge**.

---

# Technical Stack

| Technology             | Purpose                 |
| ---------------------- | ----------------------- |
| **Kotlin**             | Application development |
| **Room (SQLite)**      | Local offline database  |
| **Firebase Firestore** | Cloud synchronization   |
| **Material Design 3**  | User interface design   |
| **MPAndroidChart**     | Data visualization      |
| **Kotlin Coroutines**  | Background processing   |

---

# Documentation

The following documents are included as part of this project:

* **Research Document** *(Insert File Link)*
* **Design Document** *(Insert File Link)*

---

# Conclusion

**Cash Control** is designed to make financial management simple, interactive, and rewarding. Through secure cloud storage, intelligent analytics, budget tracking, and gamified motivation, the application empowers users to develop better financial habits and maintain long-term financial stability.

**Cash Control – Master your money, one transaction at a time.**

