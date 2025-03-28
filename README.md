# Finance Manager

A comprehensive Android application for personal finance management, budgeting, and expense tracking.

## Overview

Finance Manager helps users track their daily expenses, manage budgets, and gain insights into their spending habits. The application provides an intuitive interface for recording transactions, setting up budgets for different categories, and visualizing financial data.

## Features

- **Transaction Management**: Add, edit, and delete income and expense transactions
- **Budget Planning**: Create and manage budgets for different spending categories
- **Financial Dashboard**: View summaries and visualizations of your financial status
- **User Profiles**: Manage user information and preferences
- **Transaction Categories**: Organize transactions by customizable categories
- **Currency Formatting**: Support for different currency formats
- **Date Filtering**: Filter transactions by date ranges

## Project Structure

### Data Layer

The app follows the Room database architecture for local data persistence:

- **DAO (Data Access Object)**
  - `BudgetDao`: Interface for budget-related database operations
  - `TransactionDao`: Interface for transaction-related database operations
  - `UserDao`: Interface for user-related database operations

- **Entities**
  - `Budget`: Data model for budget information
  - `Transaction`: Data model for transaction information
  - `User`: Data model for user information

- **Repositories**
  - `BudgetRepository`: Manages budget data operations
  - `TransactionRepository`: Manages transaction data operations
  - `UserRepository`: Manages user data operations

- **Database**
  - `AppDatabase`: Main database configuration for the application

### UI Layer

The app follows MVVM (Model-View-ViewModel) architecture:

- **Adapters**
  - `BudgetAdapter`: RecyclerView adapter for displaying budgets
  - `TransactionAdapter`: RecyclerView adapter for displaying transactions

- **Fragments**
  - `AddEditBudgetFragment`: UI for adding or editing budgets
  - `AddTransactionFragment`: UI for adding new transactions
  - `BudgetFragment`: UI for displaying budget information
  - `EditTransactionFragment`: UI for editing existing transactions
  - `HomeFragment`: Main dashboard UI
  - `ProfileFragment`: UI for user profile management
  - `TransactionDetailFragment`: UI for detailed transaction view
  - `TransactionsFragment`: UI for listing all transactions

- **ViewModels**
  - `BudgetViewModel`: Business logic for budget-related operations
  - `TransactionViewModel`: Business logic for transaction-related operations
  - `UserViewModel`: Business logic for user-related operations

### Utility Classes

- `Constants`: Common constants used throughout the app
- `CurrencyFormatter`: Utility for formatting currency values
- `DateConverter`: Converter for date types in Room database
- `DateUtils`: Date-related utility functions

### Activities

- `MainActivity`: Main container activity
- `SplashActivity`: Launch screen

### Resources

- Various layouts, drawables, and menu resources for UI implementation
- Navigation components for seamless navigation between fragments

## Technical Implementation

- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room Persistence Library
- **Navigation**: Jetpack Navigation Component
- **UI Components**: Material Design components
- **Build System**: Gradle with Kotlin DSL

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Sync the Gradle files
4. Run the application on an emulator or physical device

## Requirements

- Android Studio Arctic Fox or later
- Minimum SDK version: 21 (Android 5.0 Lollipop)
- Target SDK version: 33 (Android 13)

## Dependencies

The app uses several key dependencies:

- AndroidX libraries
- Room Persistence Library
- Lifecycle components
- Material Design components
- Navigation components

For detailed dependency information, check the `libs.versions.toml` file.

## License
© 2025 Rugved Manoj Kharde, NMIMS Mumbai. All Rights Reserved.


![image](https://github.com/user-attachments/assets/67938661-4caf-45f3-ad36-5365d14e74b1) 
Splash![Splash](https://github.com/user-attachments/assets/3972d747-1e9e-4074-9d4a-e2c049fad6b5)
profile![profile](https://github.com/user-attachments/assets/46cbcaa7-c95e-4b68-a883-1e46fba6c573) 
Budget![Budget](https://github.com/user-attachments/assets/e4aa6b3f-0a48-4c8a-99ae-9e4fabe9e9ea)
Add budget![Add budget](https://github.com/user-attachments/assets/c74fe9fb-4635-4b69-9b25-674b9f39927f) 
Add Transaction![Add Transaction](https://github.com/user-attachments/assets/116c313b-7ea8-4631-803d-43d14136ba82)
Transaction Home screen![Transaction Home screen](https://github.com/user-attachments/assets/2c110b51-cfe3-40c1-9ce5-c8c6d5f125d3) 
Dashboard![Dashboard](https://github.com/user-attachments/assets/519949a9-1aeb-403e-962f-fb1e7cbc1050)
Export data![Export data](https://github.com/user-attachments/assets/90792483-2e18-468d-b6de-27170045de8b) 
data Exported![data Exported](https://github.com/user-attachments/assets/aa072ebc-79c4-4d8c-b976-0e1eba19870f)








