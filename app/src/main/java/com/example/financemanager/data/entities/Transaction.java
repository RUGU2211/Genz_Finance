package com.example.financemanager.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.financemanager.utils.DateConverter;

import java.util.Date;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String category;
    private double amount;

    @TypeConverters(DateConverter.class)
    private Date date;

    private String type; // "INCOME" or "EXPENSE"
    private String paymentMethod;
    private String notes;

    // Constructor
    public Transaction(String title, String category, double amount, Date date, String type, String paymentMethod, String notes) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}