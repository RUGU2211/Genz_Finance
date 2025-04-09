# Final Mobile Application Development
# Group Project Report

## Project Title:
Genz Finance: Personal Finance Manager App

## Team Members:
1. Rugved Manoj Kharde - Team Lead & Developer (Overall project coordination, UI design, Firebase integration)
2. Saurabh Sharma - Developer (Core functionality, transaction management, database setup)
3. Shivam Patel - Developer (Budget management, visualization, testing)

## 1. Introduction:
Genz Finance is a comprehensive Android application designed to empower users in managing their personal finances with ease and precision. The app aims to solve the problem of disorganized personal finance management by providing a centralized platform to track expenses, set budgets, and visualize spending patterns. 

Genz Finance targets young adults and professionals who want to take control of their financial health through a modern, user-friendly interface. The overall vision is to promote financial literacy and responsible spending habits through intuitive financial tracking and insights.

## 2. Design and UI (15%)

### 2.1 User Interface:
#### Screenshots of the main screens:

- **Splash Screen**: Initial loading screen with app branding
- **Login/Signup**: Authentication interface
- **Home Dashboard**: Overview of financial status with summary cards
- **Transactions**: List of all income and expense entries
- **Add Transaction**: Form to record new financial transactions
- **Budget Overview**: Visual representation of budget categories and usage
- **Profile Management**: User information and settings

#### Design Considerations:
- **Color Scheme**: Material Design color palette with primary blue tones and complementary accent colors
- **Typography**: Modern sans-serif fonts for clear readability
- **Icons**: Material Design icon set for consistent visual language
- **Layouts**: Card-based UI elements for organized information presentation

#### User Experience:
- Navigation via bottom navigation bar for main sections
- Floating action buttons for primary actions
- Pull-to-refresh for data updates
- Intuitive form layouts with appropriate input validators
- Accessible design elements with proper contrast ratios

### 2.2 User Flow:
- User registration/login → Home dashboard → Create budget → Add transactions → View insights
- The app follows a logical flow between screens, with the bottom navigation providing consistent access to main features
- Appropriate back navigation and confirmation dialogs for critical actions

## 3. Functionality (20%)

### 3.1 Core Features:

#### Transaction Management:
- Add, edit, and delete financial transactions
- Categorize transactions (e.g., food, transportation, utilities)
- Filter transactions by date, category, and type
- Search functionality for finding specific transactions

#### Budget Planning:
- Create monthly budgets for different spending categories
- Track budget utilization with visual progress indicators
- Receive alerts when approaching budget limits
- Compare actual spending against budgeted amounts

#### Financial Dashboard:
- View total balance, income, and expenses at a glance
- Visualize spending patterns through interactive charts
- Track financial progress over time
- Summary cards for quick insights

#### User Profile Management:
- Personal information management
- Profile picture customization
- Currency preferences
- Data export functionality

### 3.2 Additional Features:
- Firebase authentication for secure user access
- Cloud synchronization of financial data
- Data export for offline analysis
- Multiple currency support
- Date range filtering for reports
- Dark/light theme support

## 4. Innovation & Creativity (10%)

### 4.1 Unique Features or Approaches:

#### Intelligent Spending Insights:
The app analyzes transaction patterns and provides personalized recommendations for better financial management. Instead of just showing data, it interprets the information to suggest actionable steps.

#### Category-based Visual Analytics:
The pie chart visualization in the home screen provides an immediate understanding of spending distribution across categories, making financial patterns instantly recognizable.

#### Flexible Budget Management:
Unlike many finance apps that only offer monthly budgets, our implementation allows for custom date ranges and recurring budgets with intelligent reminders.

#### Seamless Authentication Experience:
The integration of Firebase authentication with local database caching ensures users can access their financial data instantly while maintaining strong security.

## 5. Technical Complexity (15%)

### 5.1 Technical Challenges:

#### MVVM Architecture Implementation:
The app follows the MVVM (Model-View-ViewModel) architecture pattern for clean separation of concerns. ViewModels like `TransactionViewModel` and `BudgetViewModel` handle data operations and expose LiveData objects to the UI, making the codebase maintainable and testable.

```
// TransactionViewModel.java
public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepository repository;
    private final LiveData<List<Transaction>> allTransactions;
    
    public TransactionViewModel(Application application) {
        super(application);
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        TransactionDao transactionDao = AppDatabase.getDatabase(application).transactionDao();
        repository = new TransactionRepository(transactionDao, userId);
        allTransactions = repository.getAllTransactions();
        // Additional initialization
    }
    
    // ViewModel methods that expose data to UI
}
```

#### Firebase Integration with Room Database:
The application combines local persistence using Room with cloud synchronization via Firebase Firestore, presenting challenges in data consistency and conflict resolution.

```
// Repository Pattern Implementation
public void addTransaction(Transaction transaction) {
    String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    if (userId == null) {
        error.setValue("User not authenticated");
        return;
    }

    isLoading.setValue(true);
    transaction.setUserId(userId);
    
    db.collection("transactions")
        .add(transaction)
        .addOnSuccessListener(documentReference -> {
            isLoading.setValue(false);
            transaction.setId(documentReference.getId());
            loadTransactions();
        })
        .addOnFailureListener(e -> {
            isLoading.setValue(false);
            error.setValue("Error adding transaction: " + e.getMessage());
        });
}
```

#### Data Visualization with MPAndroidChart:
Implementing interactive charts with real-time data updates required complex data transformation and event handling.

```
// Chart Implementation in HomeFragment
private void updatePieChart(List<TransactionDao.CategoryTotal> categoryTotals) {
    List<PieEntry> entries = new ArrayList<>();
    for (TransactionDao.CategoryTotal categoryTotal : categoryTotals) {
        entries.add(new PieEntry((float) categoryTotal.getTotal(), categoryTotal.getCategory()));
    }

    PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
    dataSet.setValueTextSize(12f);

    PieData data = new PieData(dataSet);
    data.setValueFormatter(new PercentFormatter(chartExpenses));
    chartExpenses.setData(data);
    chartExpenses.invalidate();
}
```

## 6. Security and Data Management (10%)

### 6.1 Data Handling:
- **Room Database**: Local data persistence using SQLite with Room ORM
- **Entity Classes**: Well-defined data models (Transaction, Budget, User)
- **Type Converters**: Custom converters for complex data types (Date, List)
- **Firebase Firestore**: Cloud database for synchronized data access

### 6.2 Security Measures:
- **Firebase Authentication**: Secure user identity management
- **Input Validation**: Form validation to prevent malformed data
- **Data Encryption**: Sensitive data storage with encryption
- **Access Control**: Role-based access to database resources
- **Secure API Calls**: Token-based authentication for network requests
- **Data Privacy**: Local data isolation between user accounts

## 7. Testing and Debugging (10%)

### 7.1 Testing Strategy:
- **Unit Testing**: JUnit tests for business logic and database operations
- **UI Testing**: Espresso tests for user interface interactions
- **Integration Testing**: Testing components working together
- **Manual Testing**: Thorough manual testing across different devices

### 7.2 Debugging Process:
- Used Logcat for runtime error identification and resolution
- Firebase Crashlytics for production crash reports
- Implemented comprehensive error handling with user-friendly messages
- Addressed layout issues on different screen sizes
- Fixed database migration challenges when schema evolved

## 8. Team Collaboration (5%)

### 8.1 Contribution Breakdown:
- **Rugved Manoj Kharde**: Project architecture, UI/UX design, Firebase integration, authentication
- **Saurabh Sharma**: Core transaction functionality, database structure, export features
- **Shivam Patel**: Budget management, data visualization, testing coordination

### 8.2 Tools for Collaboration:
- **GitHub**: Version control and code sharing
- **Trello**: Task management and project tracking
- **Discord**: Team communication and meetings
- **Figma**: Collaborative UI design
- **Google Drive**: Document sharing and reporting

## 9. Documentation (5%)
This comprehensive document serves as the project documentation, detailing all aspects of the Genz Finance app development, including architecture, functionality, design decisions, and implementation challenges.

## 10. Demo & Viva (10%)
A demonstration video has been prepared showing the app's key features:
- User authentication flow
- Transaction management
- Budget creation and tracking
- Dashboard visualization
- Profile management
- Data export functionality

## 11. Conclusion:
The Genz Finance app development process was a comprehensive exercise in modern Android application development. The team successfully implemented a feature-rich financial management application using best practices in architecture, data management, and user experience design.

Challenges faced included:
- Implementing a seamless offline-online synchronization model
- Creating intuitive data visualizations that provided meaningful insights
- Ensuring consistent user experience across different Android devices and versions

The outcome is a robust, user-friendly financial management tool that empowers users to take control of their finances. For future improvements, we would consider implementing machine learning for predictive spending analysis, adding bill reminder functionality, and supporting multiple languages and currencies.

## 12. Appendix:

### 12.1 Full Code Repository:
GitHub Repository: https://github.com/RUGU2211/Genz_Finance.git

### 12.2 References:
- Android Developer Documentation: https://developer.android.com/docs
- Firebase Documentation: https://firebase.google.com/docs
- Material Design Guidelines: https://material.io/design
- MPAndroidChart Library: https://github.com/PhilJay/MPAndroidChart
- Room Persistence Library: https://developer.android.com/training/data-storage/room 