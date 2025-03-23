package com.example.financemanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.financemanager.data.entities.Transaction;

import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByType(String type);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate);

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    LiveData<Double> getTotalExpense();

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getExpensesByCategory();

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'INCOME' GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getIncomeByCategory();

    // This class is used for the category totals queries
    class CategoryTotal {
        public String category;
        public double total;
    }
}