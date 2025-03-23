package com.example.financemanager.data.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.financemanager.data.AppDatabase;
import com.example.financemanager.data.dao.UserDao;
import com.example.financemanager.data.entities.User;

public class UserRepository {

    private final UserDao userDao;
    private final LiveData<User> user;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        userDao = database.userDao();
        user = userDao.getUser();
    }

    // Methods to access data from the database
    public LiveData<User> getUser() {
        return user;
    }

    // Operations on the database
    public void insert(User user) {
        new InsertUserAsyncTask(userDao).execute(user);
    }

    public void update(User user) {
        new UpdateUserAsyncTask(userDao).execute(user);
    }

    public void updateBalance(double balance) {
        new UpdateBalanceAsyncTask(userDao).execute(balance);
    }

    public void updateIncome(double income) {
        new UpdateIncomeAsyncTask(userDao).execute(income);
    }

    public void updateExpense(double expense) {
        new UpdateExpenseAsyncTask(userDao).execute(expense);
    }

    // AsyncTask classes for database operations
    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private final UserDao userDao;

        private InsertUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(User... users) {
            userDao.insert(users[0]);
            return null;
        }
    }

    private static class UpdateUserAsyncTask extends AsyncTask<User, Void, Void> {
        private final UserDao userDao;

        private UpdateUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(User... users) {
            userDao.update(users[0]);
            return null;
        }
    }

    private static class UpdateBalanceAsyncTask extends AsyncTask<Double, Void, Void> {
        private final UserDao userDao;

        private UpdateBalanceAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(Double... balances) {
            userDao.updateBalance(balances[0]);
            return null;
        }
    }

    private static class UpdateIncomeAsyncTask extends AsyncTask<Double, Void, Void> {
        private final UserDao userDao;

        private UpdateIncomeAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(Double... incomes) {
            userDao.updateIncome(incomes[0]);
            return null;
        }
    }

    private static class UpdateExpenseAsyncTask extends AsyncTask<Double, Void, Void> {
        private final UserDao userDao;

        private UpdateExpenseAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(Double... expenses) {
            userDao.updateExpense(expenses[0]);
            return null;
        }
    }
}