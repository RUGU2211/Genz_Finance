package com.example.financemanager.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    private int id = 1; // Single user app, so fixed ID

    private String name;
    private String email;
    private String profileImagePath;
    private double totalBalance;
    private double totalIncome;
    private double totalExpense;

    // Constructor
    public User(String name, String email, String profileImagePath, double totalBalance, double totalIncome, double totalExpense) {
        this.name = name;
        this.email = email;
        this.profileImagePath = profileImagePath;
        this.totalBalance = totalBalance;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }
}