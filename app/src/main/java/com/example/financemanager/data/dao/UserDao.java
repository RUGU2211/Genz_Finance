package com.example.financemanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.financemanager.data.entities.User;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE id = 1")
    LiveData<User> getUser();

    @Query("UPDATE users SET totalBalance = :balance WHERE id = 1")
    void updateBalance(double balance);

    @Query("UPDATE users SET totalIncome = :income WHERE id = 1")
    void updateIncome(double income);

    @Query("UPDATE users SET totalExpense = :expense WHERE id = 1")
    void updateExpense(double expense);
}