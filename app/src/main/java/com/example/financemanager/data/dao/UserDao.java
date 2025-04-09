package com.example.financemanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.financemanager.data.entities.User;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<User> getUser(String userId);

    @Query("UPDATE users SET totalBalance = :balance WHERE id = :userId")
    void updateBalance(String userId, double balance);

    @Query("UPDATE users SET totalIncome = :income WHERE id = :userId")
    void updateIncome(String userId, double income);

    @Query("UPDATE users SET totalExpense = :expense WHERE id = :userId")
    void updateExpense(String userId, double expense);

    @Query("SELECT EXISTS(SELECT 1 FROM users LIMIT 1)")
    LiveData<Boolean> checkUserExists();
}