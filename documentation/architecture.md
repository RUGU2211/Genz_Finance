# Genz Finance - Technical Architecture

## Overview

Genz Finance follows the MVVM (Model-View-ViewModel) architecture pattern with a clean separation of concerns. The application is built with modern Android development practices, utilizing Jetpack components and Firebase services.

## Architecture Layers

### 1. Presentation Layer
- **Activities & Fragments**: UI components that display data to the user
- **ViewModels**: Bridge between the UI and data layers, handling UI-related data and logic
- **Adapters**: Convert data for display in RecyclerViews and other UI components
- **XML Layouts**: Define the structure and appearance of the UI

### 2. Domain Layer
- **Use Cases/Repositories**: Implement business logic and data operations
- **Models**: Domain-specific models used throughout the application

### 3. Data Layer
- **Repositories**: Abstract data sources and provide a clean API to the domain layer
- **Local Database (Room)**: Persistent local storage for offline capabilities
- **Remote Services (Firebase)**: Cloud services for authentication and data synchronization

## Key Components

### Data Persistence
- **Room Database**: Local SQLite database with ORM features
  - **Entities**: `Transaction`, `Budget`, `User`
  - **DAOs**: Data Access Objects for database operations
  - **Type Converters**: Handle complex data types in the database

```java
@Database(entities = {User.class, Transaction.class, Budget.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    // Database implementation
}
```

### Firebase Integration
- **Authentication**: User registration, login, and session management
- **Firestore**: Cloud database for data synchronization across devices
- **Storage**: Storage for user profile images and other assets

```java
// Firebase Authentication
FirebaseAuth auth = FirebaseAuth.getInstance();

// Firestore Database
FirebaseFirestore db = FirebaseFirestore.getInstance();
```

### ViewModels
The application uses ViewModels to manage UI-related data in a lifecycle-conscious way:

- **TransactionViewModel**: Manages transaction data operations
- **BudgetViewModel**: Handles budget-related operations
- **AuthViewModel**: Manages authentication state
- **UserViewModel**: Handles user profile data

```java
public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepository repository;
    private final LiveData<List<Transaction>> allTransactions;
    
    // ViewModel implementation
}
```

### Navigation
- **Jetpack Navigation Component**: Single-activity architecture with fragment navigation
- **Bottom Navigation**: Provides access to main sections of the app

```java
// Navigation Setup
NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
        .findFragmentById(R.id.nav_host_fragment);
navController = navHostFragment.getNavController();
```

### Data Binding & LiveData
- **LiveData**: Observable data holder classes for UI updates
- **Two-way Data Binding**: Connect UI components with data sources

```java
// LiveData Observation
budgetViewModel.getAllBudgets().observe(getViewLifecycleOwner(), budgets -> {
    if (budgets != null && !budgets.isEmpty()) {
        budgetAdapter.submitList(budgets);
        // UI updates
    }
});
```

### Visualization
- **MPAndroidChart**: Library for interactive chart visualization

```java
private void updatePieChart(List<TransactionDao.CategoryTotal> categoryTotals) {
    // Chart implementation
}
```

## Data Flow

1. **User Action**: User interacts with the UI
2. **ViewModel Processing**: ViewModel processes the action
3. **Repository Operations**: Repository executes the required data operations
4. **Data Source**: Room database or Firebase services handle data persistence
5. **Response Chain**: Data flows back through the repository to the ViewModel
6. **UI Update**: LiveData observers update the UI with the new data

## Concurrency
- **Kotlin Coroutines**: Handle asynchronous operations
- **LiveData**: Thread-safe data observation

## Security
- **Firebase Authentication**: Secure user authentication
- **Data Validation**: Input validation throughout the app
- **Firestore Security Rules**: Control access to cloud data 