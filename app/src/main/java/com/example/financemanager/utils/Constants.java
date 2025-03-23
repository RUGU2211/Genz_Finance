package com.example.financemanager.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {

    // Transaction types
    public static final String TRANSACTION_TYPE_INCOME = "INCOME";
    public static final String TRANSACTION_TYPE_EXPENSE = "EXPENSE";

    // Default expense categories
    public static final List<String> DEFAULT_EXPENSE_CATEGORIES = Arrays.asList(
            "Food & Dining",
            "Transportation",
            "Rent & Utilities",
            "Groceries",
            "Entertainment",
            "Shopping",
            "Health & Medical",
            "Travel",
            "Education",
            "Personal Care",
            "Gifts & Donations",
            "Bills",
            "Investments",
            "Other"
    );

    // Default income categories
    public static final List<String> DEFAULT_INCOME_CATEGORIES = Arrays.asList(
            "Salary",
            "Freelance",
            "Investments",
            "Dividends",
            "Rental Income",
            "Gifts",
            "Refunds",
            "Other"
    );

    // Default payment methods
    public static final List<String> DEFAULT_PAYMENT_METHODS = Arrays.asList(
            "Cash",
            "Credit Card",
            "Debit Card",
            "Bank Transfer",
            "Mobile Payment",
            "Check",
            "Other"
    );

    // Budget periods
    public static final String BUDGET_PERIOD_WEEKLY = "WEEKLY";
    public static final String BUDGET_PERIOD_MONTHLY = "MONTHLY";
    public static final String BUDGET_PERIOD_YEARLY = "YEARLY";
}