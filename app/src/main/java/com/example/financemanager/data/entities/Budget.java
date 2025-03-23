package com.example.financemanager.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String category;
    private double budgetAmount;
    private double spentAmount;
    private String period; // "MONTHLY", "WEEKLY", etc.

    // Constructor
    public Budget(String category, double budgetAmount, double spentAmount, String period) {
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.period = period;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    // Helper method to calculate remaining budget
    public double getRemainingAmount() {
        return budgetAmount - spentAmount;
    }

    // Helper method to calculate percentage spent
    public double getPercentageSpent() {
        if (budgetAmount == 0) {
            return 0;
        }
        return (spentAmount / budgetAmount) * 100;
    }
}