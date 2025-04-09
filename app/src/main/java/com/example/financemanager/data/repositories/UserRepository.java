package com.example.financemanager.data.repositories;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.financemanager.data.dao.UserDao;
import com.example.financemanager.data.entities.User;

public class UserRepository {

    private final UserDao userDao;
    private final String userId;
    private LiveData<User> user;

    public UserRepository(UserDao userDao, String userId) {
        this.userDao = userDao;
        this.userId = userId;
        this.user = userDao.getUser(userId);
    }

    // Methods to access data from the database
    public LiveData<User> getUser() {
        return user;
    }

    // Operations on the database
    public void insert(User user) {
        new Thread(() -> userDao.insert(user)).start();
    }

    public void update(User user) {
        new Thread(() -> userDao.update(user)).start();
    }

    public void delete(User user) {
        new Thread(() -> userDao.delete(user)).start();
    }

    public void updateBalance(double balance) {
        userDao.updateBalance(userId, balance);
    }

    public void updateIncome(double income) {
        userDao.updateIncome(userId, income);
    }

    public void updateExpense(double expense) {
        userDao.updateExpense(userId, expense);
    }

    public LiveData<Boolean> checkUserExists() {
        return userDao.checkUserExists();
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
        private final String userId;

        private UpdateBalanceAsyncTask(UserDao userDao, String userId) {
            this.userDao = userDao;
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Double... balances) {
            userDao.updateBalance(userId, balances[0]);
            return null;
        }
    }

    private static class UpdateIncomeAsyncTask extends AsyncTask<Double, Void, Void> {
        private final UserDao userDao;
        private final String userId;

        private UpdateIncomeAsyncTask(UserDao userDao, String userId) {
            this.userDao = userDao;
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Double... incomes) {
            userDao.updateIncome(userId, incomes[0]);
            return null;
        }
    }

    private static class UpdateExpenseAsyncTask extends AsyncTask<Double, Void, Void> {
        private final UserDao userDao;
        private final String userId;

        private UpdateExpenseAsyncTask(UserDao userDao, String userId) {
            this.userDao = userDao;
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Double... expenses) {
            userDao.updateExpense(userId, expenses[0]);
            return null;
        }
    }
}