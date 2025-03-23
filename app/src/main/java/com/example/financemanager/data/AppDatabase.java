// com.example.financemanager.data.AppDatabase.java
package com.example.financemanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.financemanager.data.dao.BudgetDao;
import com.example.financemanager.data.dao.TransactionDao;
import com.example.financemanager.data.dao.UserDao;
import com.example.financemanager.data.entities.Budget;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.data.entities.User;
import com.example.financemanager.utils.DateConverter;

@Database(entities = {Transaction.class, Budget.class, User.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "finance_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract TransactionDao transactionDao();

    public abstract BudgetDao budgetDao();

    public abstract UserDao userDao();
}