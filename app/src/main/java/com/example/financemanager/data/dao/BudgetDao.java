package com.example.financemanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.financemanager.data.entities.Budget;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets")
    LiveData<List<Budget>> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE category = :category")
    LiveData<Budget> getBudgetByCategory(String category);

    @Query("SELECT SUM(budgetAmount) FROM budgets")
    LiveData<Double> getTotalBudget();
}