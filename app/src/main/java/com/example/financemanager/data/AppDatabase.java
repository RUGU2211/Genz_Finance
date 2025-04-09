// com.example.financemanager.data.AppDatabase.java
package com.example.financemanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.financemanager.data.dao.BudgetDao;
import com.example.financemanager.data.dao.TransactionDao;
import com.example.financemanager.data.dao.UserDao;
import com.example.financemanager.data.entities.Budget;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.data.entities.User;
import com.example.financemanager.utils.DateConverter;

@Database(entities = {User.class, Transaction.class, Budget.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // Migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create new users table with updated schema
            database.execSQL("CREATE TABLE IF NOT EXISTS users_new (" +
                    "id TEXT NOT NULL PRIMARY KEY," +
                    "email TEXT NOT NULL," +
                    "name TEXT," +
                    "photoUrl TEXT," +
                    "createdAt INTEGER NOT NULL)");
            
            // Copy data from old users table to new one
            database.execSQL("INSERT INTO users_new (id, email, name, photoUrl, createdAt) " +
                    "SELECT id, email, name, photoUrl, createdAt FROM users");
            
            // Drop old users table
            database.execSQL("DROP TABLE users");
            
            // Rename new table to users
            database.execSQL("ALTER TABLE users_new RENAME TO users");

            // Create transactions table if it doesn't exist
            database.execSQL("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id TEXT NOT NULL PRIMARY KEY," +
                    "userId TEXT NOT NULL," +
                    "type TEXT NOT NULL," +
                    "category TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "description TEXT," +
                    "date INTEGER NOT NULL," +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");
                    
            // Create budgets table if it doesn't exist
            database.execSQL("CREATE TABLE IF NOT EXISTS budgets (" +
                    "id TEXT NOT NULL PRIMARY KEY," +
                    "userId TEXT NOT NULL," +
                    "category TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "period TEXT NOT NULL," +
                    "startDate INTEGER NOT NULL," +
                    "endDate INTEGER NOT NULL," +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "finance_manager_db")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();

    public abstract TransactionDao transactionDao();

    public abstract BudgetDao budgetDao();
}