package com.example.financemanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
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

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions(String userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByType(String userId, String type);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDates(String userId, Date startDate, Date endDate);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type")
    LiveData<Double> getTotalByType(String userId, String type);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND category = :category ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String userId, String category);

    @Query("SELECT DISTINCT category FROM transactions WHERE userId = :userId AND type = :type")
    LiveData<List<String>> getCategoriesByType(String userId, String type);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income' AND userId = :userId")
    LiveData<Double> getTotalIncome(String userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND userId = :userId")
    LiveData<Double> getTotalExpense(String userId);

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'expense' AND userId = :userId GROUP BY category")
    LiveData<List<CategoryTotal>> getExpensesByCategory(String userId);

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'income' AND userId = :userId GROUP BY category")
    LiveData<List<CategoryTotal>> getIncomeByCategory(String userId);

    // This class is used for the category totals queries
    static class CategoryTotal {
        public String category;
        public double total;

        public Object getTotal() {
            return null;
        }

        public String getCategory() {
            return "";
        }
    }
}